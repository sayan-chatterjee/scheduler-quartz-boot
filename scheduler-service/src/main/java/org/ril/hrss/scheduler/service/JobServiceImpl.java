package org.ril.hrss.scheduler.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.job.CronJob;
import org.ril.hrss.scheduler.job.SimpleJob;
import org.ril.hrss.scheduler.model.JobResponse;
import org.ril.hrss.scheduler.util.JobUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {
    
    protected static final Logger logger = Logger.getLogger(JobServiceImpl.class.getName());
    
    @Autowired
    private JobUtil jobUtil;
    
    @Autowired
    @Lazy
    private SchedulerFactoryBean schedulerFactoryBean;
    
    @Autowired
    private ApplicationContext context;
    
    private ResponseEntity<JobResponse> processResponse(boolean status, String key, Object data) {
        if (status) {
            return jobUtil.getJobResponse(key, data);
        } else {
            return jobUtil.getJobResponse(HRSSConstantUtil.GENERIC_ERROR_KEY,
                    HRSSConstantUtil.GENERIC_ERROR_MSG);
        }
    }
    
    /**
     * Schedule a one time simple job by jobName at given date.
     */
    private boolean scheduleOneTimeJob(String jobName, String endpoint,
            Class<? extends QuartzJobBean> jobClass, Date date) {
        logger.info("Request received to scheduleOneTimeJo");
        
        String jobKey = jobName;
        String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
        String triggerKey = jobName;
        
        JobDetail jobDetail =
                jobUtil.createJob(jobClass, false, context, jobKey, groupKey, endpoint);
        
        logger.info("creating trigger for key simple job");
        Trigger cronTriggerBean = jobUtil.createSingleTrigger(triggerKey, date,
                SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.scheduleJob(jobDetail, cronTriggerBean);
            logger.info("Simple Job with key created");
            return true;
        } catch (SchedulerException e) {
            logger.info(
                    "SchedulerException while scheduling job JobServiceImpl.scheduleOneTimeJob()");
        }
        
        return false;
    }
    
    /**
     * Schedule a cron job by jobName at given cron.
     */
    private boolean scheduleCronJob(String jobName, String endpoint,
            Class<? extends QuartzJobBean> jobClass, Date date, String cronExpression) {
        logger.info("Request received to scheduleCronJob");
        
        String jobKey = jobName;
        String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
        String triggerKey = jobName;
        
        JobDetail jobDetail =
                jobUtil.createJob(jobClass, false, context, jobKey, groupKey, endpoint);
        
        logger.info("creating trigger for cron job");
        Trigger cronTriggerBean = jobUtil.createCronTrigger(triggerKey, date, cronExpression,
                SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.scheduleJob(jobDetail, cronTriggerBean);
            logger.info("Cron Job with key created");
            return true;
        } catch (SchedulerException e) {
            logger.info("SchedulerException while scheduling job JobServiceImpl.scheduleCronJob()");
        }
        
        return false;
    }
    
    /**
     * Schedule a job by jobName at given date.
     */
    @Override
    public ResponseEntity<JobResponse> scheduleJob(String jobName, String endpoint,
            Date jobScheduleTime, String cronExpression) {
        // Check if job Name is unique;
        if (!this.isJobWithNamePresent(jobName)) {
            if (StringUtils.isEmpty(cronExpression.trim())) {
                // Single Trigger
                boolean status = this.scheduleOneTimeJob(jobName, endpoint, SimpleJob.class,
                        jobScheduleTime);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        this.getAllJobs());
            } else {
                // Cron Trigger
                boolean status = this.scheduleCronJob(jobName, endpoint, CronJob.class,
                        jobScheduleTime, cronExpression);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        this.getAllJobs());
            }
            
        } else {
            return jobUtil.getJobResponse(HRSSConstantUtil.JOB_WITH_SAME_NAME_EXIST_KEY,
                    HRSSConstantUtil.JOB_WITH_SAME_NAME_EXIST_MSG);
        }
    }
    
    /**
     * Update one time scheduled job.
     */
    private boolean updateOneTimeJob(String jobName, Date date) {
        logger.info("Request received for updating one time job.");
        String jobKey = jobName;
        
        try {
            Trigger newTrigger = jobUtil.createSingleTrigger(jobKey, date,
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
            
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey),
                    newTrigger);
            logger.info("Trigger update one time job associated with jobKey");
            return true;
        } catch (Exception e) {
            logger.info("SchedulerException while updating one time job");
            return false;
        }
    }
    
    /**
     * Update scheduled cron job.
     */
    private boolean updateCronJob(String jobName, Date date, String cronExpression) {
        logger.info("Request received for updating cron job.");
        String jobKey = jobName;
        
        try {
            Trigger newTrigger = jobUtil.createCronTrigger(jobKey, date, cronExpression,
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
            
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey),
                    newTrigger);
            logger.info("Trigger update cron job associated with jobKey");
            return true;
        } catch (Exception e) {
            logger.info("SchedulerException while updating cron job");
            return false;
        }
    }
    
    /**
     * Update Job
     */
    @Override
    public ResponseEntity<JobResponse> updateJob(String jobName, Date jobScheduleTime,
            String cronExpression) {
        // Edit Job
        if (this.isJobWithNamePresent(jobName)) {
            
            if (StringUtils.isNotEmpty(cronExpression.trim())) {
                
                // Cron Trigger
                boolean status = this.updateCronJob(jobName, jobScheduleTime, cronExpression);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        this.getAllJobs());
            } else {
                // Single Trigger
                boolean status = this.updateOneTimeJob(jobName, jobScheduleTime);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        this.getAllJobs());
            }
            
        } else {
            return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY,
                    HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
        }
    }
    
    /**
     * Remove the indicated Trigger from the scheduler. If the related job does not have any other
     * triggers, and the job is not durable, then the job will also be deleted.
     */
    @Override
    public boolean unScheduleJob(String jobName) {
        logger.info("Request received for Unscheduleding job.");
        
        String jobKey = jobName;
        TriggerKey tkey = new TriggerKey(jobKey);
        
        try {
            boolean status = schedulerFactoryBean.getScheduler().unscheduleJob(tkey);
            logger.info("Trigger associated with job for unscheduling");
            return status;
        } catch (SchedulerException e) {
            logger.info("SchedulerException while unscheduling job");
            return false;
        }
    }
    
    /**
     * Deletes a job
     */
    private boolean delete(String jobName) {
        logger.info("Request received for deleting job.");
        
        String jobKey = jobName;
        String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
        
        JobKey jkey = new JobKey(jobKey, groupKey);
        try {
            boolean status = schedulerFactoryBean.getScheduler().deleteJob(jkey);
            logger.info("Job with jobKey deleted");
            return status;
        } catch (SchedulerException e) {
            logger.info("SchedulerException while deleting job");
            return false;
        }
    }
    
    /**
     * Delete the identified Job from the Scheduler - and any associated Triggers.
     */
    @Override
    public ResponseEntity<JobResponse> deleteJob(String jobName) {
        if (this.isJobWithNamePresent(jobName)) {
            boolean isJobRunning = this.isJobRunning(jobName);
            
            if (!isJobRunning) {
                boolean status = this.delete(jobName);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        HRSSConstantUtil.SUCCESS_MSG);
            } else {
                return jobUtil.getJobResponse(HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_KEY,
                        HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_MSG);
            }
        } else {
            // Job doesn't exist
            return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY,
                    HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
        }
    }
    
    private boolean pause(String jobName) {
        logger.info("Request received for pausing job.");
        
        String jobKey = jobName;
        String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
        JobKey jkey = new JobKey(jobKey, groupKey);
        
        try {
            schedulerFactoryBean.getScheduler().pauseJob(jkey);
            logger.info("Job with jobKey paused");
            return true;
        } catch (SchedulerException e) {
            logger.info("SchedulerException while pausing job");
            return false;
        }
    }
    
    /**
     * Pause a job
     */
    @Override
    public ResponseEntity<JobResponse> pauseJob(String jobName) {
        if (this.isJobWithNamePresent(jobName)) {
            boolean isJobRunning = this.isJobRunning(jobName);
            
            if (!isJobRunning) {
                boolean status = this.pause(jobName);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        HRSSConstantUtil.SUCCESS_MSG);
            } else {
                return jobUtil.getJobResponse(HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_KEY,
                        HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_MSG);
            }
            
        } else {
            // Job doesn't exist
            return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY,
                    HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
        }
    }
    
    public boolean resume(String jobName) {
        logger.info("Request received for resuming job.");
        
        String jobKey = jobName;
        String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
        
        JobKey jKey = new JobKey(jobKey, groupKey);
        try {
            schedulerFactoryBean.getScheduler().resumeJob(jKey);
            logger.info("Job with jobKey resumed succesfully.");
            return true;
        } catch (SchedulerException e) {
            logger.info("SchedulerException while resuming job");
            return false;
        }
    }
    
    /**
     * Resume paused job
     */
    @Override
    public ResponseEntity<JobResponse> resumeJob(String jobName) {
        if (this.isJobWithNamePresent(jobName)) {
            String jobState = this.getJobState(jobName);
            
            if (HRSSConstantUtil.JOB_STATE_PAUSED.equals(jobState)) {
                logger.info("Job current state is PAUSED, Resuming job...");
                boolean status = this.resume(jobName);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        HRSSConstantUtil.SUCCESS_MSG);
            } else {
                return jobUtil.getJobResponse(HRSSConstantUtil.JOB_NOT_IN_PAUSED_STATE_KEY,
                        HRSSConstantUtil.JOB_NOT_IN_PAUSED_STATE_MSG);
            }
            
        } else {
            // Job doesn't exist
            return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY,
                    HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
        }
    }
    
    /**
     * Start a job instantly
     */
    private boolean startJobImmediate(String jobName) {
        logger.info("Request received for starting job now. startJobImmediate()");
        
        String jobKey = jobName;
        String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
        
        JobKey jKey = new JobKey(jobKey, groupKey);
        try {
            schedulerFactoryBean.getScheduler().triggerJob(jKey);
            logger.info("Job with jobKey started now succesfully.");
            return true;
        } catch (SchedulerException e) {
            logger.info("SchedulerException while starting job now");
            return false;
        }
    }
    
    /**
     * Start a job now
     */
    @Override
    public ResponseEntity<JobResponse> startJobNow(String jobName) {
        if (this.isJobWithNamePresent(jobName)) {
            
            if (!this.isJobRunning(jobName)) {
                boolean status = this.startJobImmediate(jobName);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        HRSSConstantUtil.SUCCESS_MSG);
                
            } else {
                // Job already running
                return jobUtil.getJobResponse(HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_KEY,
                        HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_MSG);
            }
            
        } else {
            // Job doesn't exist
            return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY,
                    HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
        }
    }
    
    /**
     * Check if job is already running
     */
    @Override
    public boolean isJobRunning(String jobName) {
        logger.info("Request received to check if job is running");
        
        String jobKey = jobName;
        String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
        try {
            List<JobExecutionContext> currentJobs =
                    schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
            currentJobs = (List<JobExecutionContext>) CollectionUtils.emptyIfNull(currentJobs);
            for (JobExecutionContext jobCtx : currentJobs) {
                String jobNameDB = jobCtx.getJobDetail().getKey().getName();
                String groupNameDB = jobCtx.getJobDetail().getKey().getGroup();
                if (jobKey.equalsIgnoreCase(jobNameDB) && groupKey.equalsIgnoreCase(groupNameDB)) {
                    return true;
                }
            }
            
        } catch (SchedulerException e) {
            logger.info("SchedulerException while checking if job is running");
            return false;
        }
        return false;
    }
    
    /**
     * Get all jobs
     */
    @Override
    public List<Map<String, Object>> getAllJobs() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    
                    String jobName = jobKey.getName();
                    String jobGroup = jobKey.getGroup();
                    
                    // get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    Date scheduleTime = triggers.get(0).getStartTime();
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    Date lastFiredTime = triggers.get(0).getPreviousFireTime();
                    
                    Map<String, Object> map = new HashMap<>();
                    map.put(HRSSConstantUtil.JOB_NAME, jobName);
                    map.put(HRSSConstantUtil.JOB_GRP_NAME, jobGroup);
                    map.put(HRSSConstantUtil.JOB_SCHDLD_TIME, scheduleTime);
                    map.put(HRSSConstantUtil.JOB_LASTFIRE_TIME, lastFiredTime);
                    map.put(HRSSConstantUtil.JOB_NXTFIRE_TIME, nextFireTime);
                    
                    if (isJobRunning(jobName)) {
                        map.put(HRSSConstantUtil.JOB_STATUS, HRSSConstantUtil.JOB_STATE_RUNNING);
                    } else {
                        String jobState = getJobState(jobName);
                        map.put(HRSSConstantUtil.JOB_STATUS, jobState);
                    }
                    
                    list.add(map);
                }
                
            }
        } catch (SchedulerException e) {
            logger.info("SchedulerException while fetching all jobs.");
        }
        return list;
    }
    
    /**
     * Check job exist with given name
     */
    @Override
    public boolean isJobWithNamePresent(String jobName) {
        try {
            String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
            JobKey jobKey = new JobKey(jobName, groupKey);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            if (scheduler.checkExists(jobKey)) {
                return true;
            }
        } catch (SchedulerException e) {
            logger.info("SchedulerException while checking job with name exist");
        }
        return false;
    }
    
    /**
     * Get the current state of job
     */
    public String getJobState(String jobName) {
        logger.info("JobServiceImpl.getJobState()");
        
        try {
            String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
            JobKey jobKey = new JobKey(jobName, groupKey);
            
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
            if (CollectionUtils.isNotEmpty(triggers)) {
                for (Trigger trigger : triggers) {
                    TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    switch (triggerState) {
                        case PAUSED:
                            return HRSSConstantUtil.JOB_STATE_PAUSED;
                        case BLOCKED:
                            return HRSSConstantUtil.JOB_STATE_BLOCKED;
                        case COMPLETE:
                            return HRSSConstantUtil.JOB_STATE_COMPLETE;
                        case ERROR:
                            return HRSSConstantUtil.JOB_STATE_ERROR;
                        case NONE:
                            return HRSSConstantUtil.JOB_STATE_NONE;
                        case NORMAL:
                            return HRSSConstantUtil.JOB_STATE_SCHEDULED;
                        default:
                            return HRSSConstantUtil.JOB_STATE_NONE;
                        
                    }
                    
                }
            }
        } catch (SchedulerException e) {
            logger.info("SchedulerException while checking job state");
        }
        return null;
    }
    
    /**
     * Stop Job
     */
    private boolean stop(String jobName) {
        logger.info("JobServiceImpl.stop()");
        try {
            String jobKey = jobName;
            String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
            
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobKey jkey = new JobKey(jobKey, groupKey);
            
            return scheduler.interrupt(jkey);
            
        } catch (SchedulerException e) {
            logger.info("SchedulerException while stopping job.");
        }
        return false;
    }
    
    /**
     * Stop a job
     */
    @Override
    public ResponseEntity<JobResponse> stopJob(String jobName) {
        logger.info("JobServiceImpl.stopJob()");
        if (this.isJobWithNamePresent(jobName)) {
            
            if (this.isJobRunning(jobName)) {
                boolean status = this.stop(jobName);
                return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY,
                        HRSSConstantUtil.SUCCESS_MSG);
            } else {
                // Job not in running state
                return jobUtil.getJobResponse(HRSSConstantUtil.JOB_NOT_IN_RUNNING_STATE_KEY,
                        HRSSConstantUtil.JOB_NOT_IN_RUNNING_STATE_MSG);
            }
            
        } else {
            // Job doesn't exist
            return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY,
                    HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
        }
    }
    
}
