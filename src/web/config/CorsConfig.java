package web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/02/28/23:08
 **/
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    static final String ORIGINS[] = new String[]{"GET", "POST", "PUT", "DELETE"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // ?????????????????????
                .allowedOriginPatterns("*") // ????????????? ???localhost??????????????????????????localhost?127.0.0.1????
                .allowCredentials(true) // ??????????
                .allowedMethods(ORIGINS) // ????????????????
                .maxAge(3600); // ???????1??? ???????
    }
}
