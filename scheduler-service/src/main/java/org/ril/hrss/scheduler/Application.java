package org.ril.hrss.scheduler;

import org.ril.hrss.msf.exception.controller.RequestControllerAdvice;
import org.ril.hrss.msf.util.ObjectMapperUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableEurekaClient
@EnableCircuitBreaker
@SpringBootApplication
@Import({ ObjectMapperUtil.class, RequestControllerAdvice.class })
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
