package web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/04/28/12:32
 **/
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfiguration {
    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        Docket docket=new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        //.title("swagger-bootstrap-ui-demo RESTful APIs")
                        .description("# spadas APIs")
                        .termsOfServiceUrl("http://shengwang.site/spadas")
                        .version("1.0")
                        .build())
                .groupName("1.0")
                .select()
                .apis(RequestHandlerSelectors.basePackage("web.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
}
