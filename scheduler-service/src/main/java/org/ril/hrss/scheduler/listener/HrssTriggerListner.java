package org.ril.hrss.scheduler.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.springframework.stereotype.Component;

@Component
public class HrssTriggerListner implements TriggerListener {
    
    protected static final Logger logger = Logger.getLogger(HrssTriggerListner.class.getName());
    
    @Override
    public String getName() {
        return HRSSConstantUtil.GLOBAL_TRIGGER_KEY;
    }
    
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        logger.info("HrssTriggerListner.triggerFired()...");
    }
    
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }
    
    @Override
    public void triggerMisfired(Trigger trigger) {
        logger.info("HrssTriggerListner.triggerMisfired()...");
        String jobName = trigger.getJobKey().getName();
        logger.log(Level.INFO, "jobName... %s", jobName);
    }
    
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        logger.info("HrssTriggerListner.triggerComplete()...");
    }
}
