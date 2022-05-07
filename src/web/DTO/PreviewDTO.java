package web.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/03/20/20:51
 **/
@Data
public class PreviewDTO {
    List<Integer> ids = new ArrayList<>();
    int rows;
}
