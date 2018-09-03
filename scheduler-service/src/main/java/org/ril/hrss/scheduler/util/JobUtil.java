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
     * @param jobClass Class whose executeInternal() method needs to be called.
     * @param isDurable Job needs to be persisted even after completion. if true, job will be
     *        persisted, not otherwise.
     * @param context Spring application context.
     * @param jobName Job name.
     * @param jobGroup Job group.
     * 
     * @return JobDetail object
     */
    public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable,
            ApplicationContext context, String jobName, String jobGroup, String endpoint) {
        logger.info("JobUtil.createJob()");
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(isDurable);
        factoryBean.setApplicationContext(context);
        factoryBean.setName(jobName);
        factoryBean.setGroup(jobGroup);
        
        // set job data map
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("endpoint", endpoint);
        factoryBean.setJobDataMap(jobDataMap);
        
        factoryBean.afterPropertiesSet();
        
        return factoryBean.getObject();
    }
    
    /**
     * Create cron trigger.
     * 
     * @param triggerName Trigger name.
     * @param startTime Trigger start time.
     * @param cronExpression Cron expression.
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens).
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
     * @param triggerName Trigger name.
     * @param startTime Trigger start time.
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens).
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
        restTemplate.getForObject(endpoint, String.class);
    }
    
    public String createURL(String endpoint, List<NameValuePair> nameValuePairs) {
        URI batchJobUri;
        URL batchJobUrl = null;
        URIBuilder builder = new URIBuilder().setScheme(HRSSConstantUtil.HTTP_PROTOCOL)
                .setHost(endpoint).setParameters(nameValuePairs);
        
        try {
            batchJobUri = builder.build();
            batchJobUrl = batchJobUri.toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            logger.info("URL exception in createURL()");
        }
        return (null != batchJobUrl) ? batchJobUrl.toString() : HRSSConstantUtil.EMPTY_STRING;
    }
    
}
