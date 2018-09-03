package org.ril.hrss.scheduler.config;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ComponentScan("org.ril.hrss.scheduler")
public class ApplicationConfiguration extends WebMvcConfigurerAdapter {
    
    @Bean
    public Docket candidateApi() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.basePackage("org.ril.hrss.scheduler.api"))
                .paths(regex("/*.*")).build().apiInfo(metaData());
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    
    private ApiInfo metaData() {
        return new ApiInfo("HR SaaS Microservices Framework",
                "HRSS Scheduler Service API - is a RESTful API that provides Scheduler details of HR SaaS. Below is a list of available REST API calls for Scheduler Service details, ",
                "1.0", "Terms of service",
                new Contact("Author", "http://www.ril.com/", "dnyaneshwar.bhirud@zmail.ril.com"),
                "Apache License Version 2.0", "https://www.apache.org/licenses/LICENSE-2.0");
    }
    
}
