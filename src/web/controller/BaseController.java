package web.controller;

import au.edu.rmit.trajectory.clustering.kmeans.indexNode;
import com.github.xiaoymin.knife4j.annotations.DynamicParameter;
import com.github.xiaoymin.knife4j.annotations.DynamicResponseParameters;
import edu.nyu.dss.similarity.Framework;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.DTO.PreviewDTO;
import web.DTO.dsqueryDTO;
import web.DTO.rangequeryDTO;
import web.Utils.FileUtil;
import web.VO.DatasetVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Tian Qi Qing
 * @version 1.0
 **/

@Api
@RestController
public class BaseController {
    @Autowired
    private FileUtil fileService;

    @ApiOperation("load dataset")
    @DynamicResponseParameters(name = "LoadDataHashMapModel",properties = {
            @DynamicParameter(name = "nodes",value = "dataset node",dataTypeClass = indexNode.class),
            @DynamicParameter(name = "filenames",value = "dataset's id-filename map"),
    })
    @RequestMapping(value = "/loaddata",method = RequestMethod.GET)
    public Map<String,Object> loadData() {
        return new HashMap(){{put("nodes", Framework.indexNodes); put("filenames",Framework.datasetIdMapping); }};
    }

    @ApiOperation("get dataset by id")
    @RequestMapping(value = "/getds",method = RequestMethod.GET)
    @ApiImplicitParam(name = "id",value = "dataset id")
    @DynamicResponseParameters(name = "SingleDatasetDataVO",properties = {
            @DynamicParameter(name = "node",value = "node summary",dataTypeClass = DatasetVo.class),
    })
    public Map<String,Object> getDatasetById(@RequestParam int id) {
        return new HashMap(){{put("node", new DatasetVo(){{
            setNode(Framework.indexNodes.get(id));
            setId(id);
            setMatrix(Framework.dataMapPorto.get(id));
            setFilename(Framework.datasetIdMapping.get(id));
        }}
        );}};
    }

    @ApiOperation("range query")
    @DynamicResponseParameters(name = "RangeQueryModel",properties = {
            @DynamicParameter(name = "nodes",value = "list of indexNode",dataTypeClass = indexNode.class),
    })
    @RequestMapping(value = "/rangequery",method = RequestMethod.POST)
    public Map<String,Object> rangequery(@RequestBody rangequeryDTO qo) {
        return new HashMap(){{put("nodes",Framework.rangequery(qo));}};
    }

    @ApiOperation("upload dataset")
    @DynamicResponseParameters(name = "UploadModel",properties = {
            @DynamicParameter(name = "matrix",value = "2d array of uploaded dataset matrix",dataTypeClass = indexNode.class),
    })
    @RequestMapping(value = "/uploaddataset",method = RequestMethod.POST)
    public Map<String,Object> uploadDataset(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = fileService.uploadFile(file);
        Framework.datalakeID=4;
        double [][] matrix = Framework.readSingleFile("/argoverse/"+filename);
        return new HashMap(){{put("matrix",matrix);}};
    }

    @ApiOperation("query by dataset")
    @DynamicResponseParameters(name = "DatasetSearchModel",properties = {
            @DynamicParameter(name = "nodes",value = "list of DatasetVO",dataTypeClass = DatasetVo.class),
    })
    @RequestMapping(value = "/dsquery",method = RequestMethod.POST)
    public Map<String,Object> datasetQuery(@RequestBody dsqueryDTO qo) throws IOException {
        List<DatasetVo> result =  Framework.datasetQuery(qo);
        return new HashMap(){{put("nodes",result);}};
    }

    @ApiOperation("download dataset")
    @GetMapping("/file/{filename:.+}")
    public void downloadFile(@PathVariable String filename, HttpServletRequest request, HttpServletResponse response) {
        fileService.downloadFile(filename, request, response);
    }

    @ApiOperation("data preview after join or union")
    @DynamicResponseParameters(name = "PreviewModel",properties = {
            @DynamicParameter(name = "headers",value = "headers of csv, Type: List<String[]>"),
            @DynamicParameter(name = "bodies",value = "bodies of csv, Type: List<String[][]>"),
    })
    @PostMapping("/preview")
    public Map<String,Object> datasetPreview(@RequestBody PreviewDTO dto) {
        List<String[]> headers = new ArrayList<>();
        List<String[][]> bodies = new ArrayList<>();
        dto.getIds().forEach(id->{
            try {
                Pair<String[],String[][]> pair = fileService.readPreviewDataset(Framework.datasetIdMapping.get(id),dto.getRows());
                headers.add(pair.getLeft());
                bodies.add(pair.getRight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return new HashMap(){{put("headers",headers);put("bodies",bodies);}};
    }


    @ApiOperation("dataset join")
    @DynamicResponseParameters(name = "JoinModel",properties = {
            @DynamicParameter(name = "header",value = "header of join data, Type: String[]"),
            @DynamicParameter(name = "joinData",value = "body of join data, Type: List<String[]>"),
    })
    @GetMapping("/join")
    public Map<String,Object> datasetJoin(@RequestParam int queryid,@RequestParam int datasetid) throws IOException {
        Pair<ArrayList<Double>,Map<Integer, Integer>> pair = Framework.pairwiseJoin(queryid,datasetid);
        Pair<String[],String[][]> querydata = fileService.readPreviewDataset(Framework.datasetIdMapping.get(queryid),Integer.MAX_VALUE);
        Pair<String[],String[][]> basedata = fileService.readPreviewDataset(Framework.datasetIdMapping.get(datasetid),Integer.MAX_VALUE);

        //int len = querydata.getRight()[0].length+basedata.getRight()[0].length;
        String[] joinHeader = ArrayUtils.addAll(querydata.getLeft(), basedata.getLeft());
        List<String[]> joindata = pair.getRight().entrySet().stream()
                .map(idPair-> ArrayUtils.addAll(querydata.getRight()[idPair.getKey()],basedata.getRight()[idPair.getValue()])).collect(Collectors.toList());
        return new HashMap(){{put("header",joinHeader);put("joinData",joindata);}};
    }

    @ApiOperation("union range search")
    @DynamicResponseParameters(name = "UnionModel",properties = {
            @DynamicParameter(name = "header",value = "header of join data, Type: String[]"),
            @DynamicParameter(name = "joinData",value = "body of join data, Type: List<String[]>"),
    })
    @RequestMapping(value = "/union",method = RequestMethod.POST)
    public Map<String,Object> datasetUnion(@RequestBody dsqueryDTO qo) throws IOException {
        Framework.UnionRangeQuery(qo.getQuerydata(),qo.getDatasetId(),2);
        return new HashMap(){{put("nodes",Framework.indexNodes.get(1));}};

    }



//    @RequestMapping(value = "/test",method = RequestMethod.GET)
//    public Map<String,Object> test1() throws IOException {
//        return new HashMap(){{put("nodes",Framework.indexNodes.get(1));}};
//
//    }




}
