package org.ril.hrss.scheduler.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ril.hrss.scheduler.model.JobResponse;
import org.springframework.http.ResponseEntity;

public interface JobService {
    
    public ResponseEntity<JobResponse> scheduleJob(String jobName, String endpoint,
            Date jobScheduleTime, String cronExpression);
    
    public ResponseEntity<JobResponse> updateJob(String jobName, Date jobScheduleTime,
            String cronExpression);
    
    public boolean unScheduleJob(String jobName);
    
    public ResponseEntity<JobResponse> deleteJob(String jobName);
    
    public ResponseEntity<JobResponse> pauseJob(String jobName);
    
    public ResponseEntity<JobResponse> resumeJob(String jobName);
    
    public ResponseEntity<JobResponse> startJobNow(String jobName);
    
    public ResponseEntity<JobResponse> stopJob(String jobName);
    
    public boolean isJobRunning(String jobName);
    
    public List<Map<String, Object>> getAllJobs();
    
    public boolean isJobWithNamePresent(String jobName);
    
    public String getJobState(String jobName);
    
}
