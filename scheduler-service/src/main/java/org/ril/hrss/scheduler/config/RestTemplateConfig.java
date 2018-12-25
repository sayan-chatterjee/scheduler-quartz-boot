package org.ril.hrss.scheduler.config;

import java.util.Collections;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@EnableAsync
@Configuration
public class RestTemplateConfig {

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
			request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			request.getHeaders().setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			return execution.execute(request, body);
		});
		return restTemplate;
	}
}
