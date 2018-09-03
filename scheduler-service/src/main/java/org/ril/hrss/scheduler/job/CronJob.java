package org.ril.hrss.scheduler.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.entity.BatchDetails;
import org.ril.hrss.scheduler.repository.BatchDetailsRepository;
import org.ril.hrss.scheduler.util.JobUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class CronJob extends QuartzJobBean implements InterruptableJob {
    
    protected static final Logger logger = Logger.getLogger(CronJob.class.getName());
    
    @Autowired
    private JobUtil jobUtil;
    
    @Autowired
    private BatchDetailsRepository batchDetailsRepository;
    
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException {
        
        logger.info("========== Cron Job started ==========");
        /*********** For retrieving stored key-value pairs ***********/
        JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
        
        String endpoint = dataMap.getString(HRSSConstantUtil.SERVICE_ENDPOINT);
        String jobName = jobExecutionContext.getJobDetail().getKey().getName();
        
        BatchDetails batchdetails =
                new BatchDetails(null, jobName, endpoint, HRSSConstantUtil.BATCH_STATUS_INPROGRESS,
                        new Date(), null, UUID.randomUUID().toString());
        batchdetails = batchDetailsRepository.save(batchdetails);
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair(HRSSConstantUtil.QRY_PARAM_BATCH_UUID,
                batchdetails.getBatchUuid()));
        jobUtil.callGETEndpoint(jobUtil.createURL(endpoint, nameValuePairs));
        
        logger.info("========== Cron Job ended with key ==========");
    }
    
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        logger.info("Stopping job... ");
    }
    
}
