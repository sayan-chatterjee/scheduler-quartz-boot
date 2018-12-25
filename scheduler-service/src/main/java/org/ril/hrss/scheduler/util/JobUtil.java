package org.ril.hrss.scheduler.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.config.PersistableCronTriggerFactoryBean;
import org.ril.hrss.scheduler.constant.CreateJobResponse;
import org.ril.hrss.scheduler.model.JobResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class JobUtil {

	protected static final Logger logger = Logger.getLogger(JobUtil.class.getName());

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Create Quartz Job.
	 * 
	 * @param jobClass
	 *            Class whose executeInternal() method needs to be called.
	 * @param isDurable
	 *            Job needs to be persisted even after completion. if true, job will
	 *            be persisted, not otherwise.
	 * @param context
	 *            Spring application context.
	 * @param jobName
	 *            Job name.
	 * @param jobGroup
	 *            Job group.
	 * 
	 * @return JobDetail object
	 */
	public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable, ApplicationContext context,
			String jobName, String jobGroup, String httpMethod, String endpoint, String payload) {
		logger.info("JobUtil.createJob()");
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(jobClass);
		factoryBean.setDurability(isDurable);
		factoryBean.setApplicationContext(context);
		factoryBean.setName(jobName);
		factoryBean.setGroup(jobGroup);

		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(HRSSConstantUtil.SERVICE_ENDPOINT, endpoint);
		jobDataMap.put(HRSSConstantUtil.SERVICE_METHOD, httpMethod);
		jobDataMap.put(HRSSConstantUtil.SERVICE_PAYLOAD, payload);
		factoryBean.setJobDataMap(jobDataMap);

		factoryBean.afterPropertiesSet();

		return factoryBean.getObject();
	}

	/**
	 * Create cron trigger.
	 * 
	 * @param triggerName
	 *            Trigger name.
	 * @param startTime
	 *            Trigger start time.
	 * @param cronExpression
	 *            Cron expression.
	 * @param misFireInstruction
	 *            Misfire instruction (what to do in case of misfire happens).
	 * 
	 * @return Trigger
	 */
	public Trigger createCronTrigger(String triggerName, Date startTime, String cronExpression,
			int misFireInstruction) {
		PersistableCronTriggerFactoryBean factoryBean = new PersistableCronTriggerFactoryBean();
		factoryBean.setName(triggerName);
		factoryBean.setStartTime(startTime);
		factoryBean.setCronExpression(cronExpression);
		factoryBean.setMisfireInstruction(misFireInstruction);
		try {
			factoryBean.afterPropertiesSet();
		} catch (ParseException e) {
			logger.info("Parsing Exception.. JobUtil.createCronTrigger()");
		}
		return factoryBean.getObject();
	}

	/**
	 * Create a Single trigger.
	 * 
	 * @param triggerName
	 *            Trigger name.
	 * @param startTime
	 *            Trigger start time.
	 * @param misFireInstruction
	 *            Misfire instruction (what to do in case of misfire happens).
	 * 
	 * @return Trigger
	 */
	public Trigger createSingleTrigger(String triggerName, Date startTime, int misFireInstruction) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setName(triggerName);
		factoryBean.setStartTime(startTime);
		factoryBean.setMisfireInstruction(misFireInstruction);
		factoryBean.setRepeatCount(0);
		factoryBean.afterPropertiesSet();
		return factoryBean.getObject();
	}

	public ResponseEntity<JobResponse> getJobResponse(String key, Object data) {
		JobResponse serverResponse = new JobResponse();
		serverResponse.setData(data);
		return CreateJobResponse.valueOf(key).createResponse(serverResponse);
	}

	@Async
	public void callGETEndpoint(String endpoint) {
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.TEXT_PLAIN);

		HttpEntity<String> httpEntity = new HttpEntity<>(header);
		this.processResponse(restTemplate.exchange(endpoint, HttpMethod.GET, httpEntity, String.class));
		logger.info("Response received from callGETEndpoint()");
	}

	@Async
	public void callPOSTEndpoint(String endpoint, String payload) {
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.TEXT_PLAIN);

		HttpEntity<String> httpEntity = new HttpEntity<>(payload, header);
		this.processResponse(restTemplate.exchange(endpoint, HttpMethod.POST, httpEntity, String.class));
		logger.info("Response from callPOSTEndpoint()");

	}

	@Async
	public void callPUTEndpoint(String endpoint, String payload) {
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.TEXT_PLAIN);

		HttpEntity<String> httpEntity = new HttpEntity<>(payload, header);
		this.processResponse(restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class));
		logger.info("Response from callPUTEndpoint()");
	}

	@Async
	public void callDELETEEndpoint(String endpoint) {
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.TEXT_PLAIN);

		HttpEntity<String> httpEntity = new HttpEntity<>(header);
		this.processResponse(restTemplate.exchange(endpoint, HttpMethod.DELETE, httpEntity, String.class));
		logger.info("Response from callDELETEEndpoint()");

	}

	public String createURL(String endpoint, List<NameValuePair> nameValuePairs) {
		URI batchJobUri;
		URL batchJobUrl = null;
		URIBuilder builder = new URIBuilder().setScheme(HRSSConstantUtil.HTTP_PROTOCOL).setHost(endpoint)
				.setParameters(nameValuePairs);

		try {
			batchJobUri = builder.build();
			batchJobUrl = batchJobUri.toURL();
		} catch (URISyntaxException | MalformedURLException e) {
			logger.info("URL exception in createURL()");
		}
		return (null != batchJobUrl) ? batchJobUrl.toString() : HRSSConstantUtil.EMPTY_STRING;
	}

	private String processResponse(ResponseEntity<String> response) {
		logger.info("JobUtil.processResponse()");
		String feed = null;
		HttpStatus responseCode = response.getStatusCode();
		switch (responseCode) {
		case OK:
			feed = response.getBody();
			break;
		case BAD_REQUEST:
			logger.info(HRSSConstantUtil.BAD_REQUEST_MSG);
			break;
		case UNAUTHORIZED:
			logger.info(HRSSConstantUtil.UNAUTHORISED_MSG);
			break;
		default:
			logger.info(HRSSConstantUtil.UNEXPECTED_ERROR_OCCURRED);
		}
		return feed;

	}

}
