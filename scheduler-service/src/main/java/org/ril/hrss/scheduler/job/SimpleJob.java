package org.ril.hrss.scheduler.job;

import java.util.logging.Logger;

import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.util.JobUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class SimpleJob extends QuartzJobBean implements InterruptableJob {
    
    protected static final Logger logger = Logger.getLogger(SimpleJob.class.getName());
    
    @Autowired
    private JobUtil jobUtil;
    
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException {
        logger.info("========== Simple Job started ==========");
        /*********** For retrieving stored key-value pairs **********/
        JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();
        String endpoint = dataMap.getString(HRSSConstantUtil.SERVICE_ENDPOINT);
        
        jobUtil.callGETEndpoint(endpoint);
        
        logger.info("========== Simple Job ended with key ==========");
    }
    
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        logger.info("Stopping job... ");
    }
    
}
