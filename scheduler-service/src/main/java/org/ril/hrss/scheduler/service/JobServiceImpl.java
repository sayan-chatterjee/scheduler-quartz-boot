package org.ril.hrss.scheduler.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.ril.hrss.scheduler.model.JobResponse;
import org.ril.hrss.scheduler.util.SchedulerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class JobServiceImpl implements JobService {

	protected static final Logger logger = Logger.getLogger(JobServiceImpl.class.getName());

	@Autowired
	private SchedulerUtil schedulerUtil;

	@Override
	public ResponseEntity<JobResponse> scheduleJob(String jobName, String endpoint, Date jobScheduleTime,
			String cronExpression, String payload, String httpMethod) {
		logger.info("JobServiceImpl.scheduleJob(with payload)...");
		return schedulerUtil.scheduleJob(jobName, endpoint, jobScheduleTime, cronExpression, payload, httpMethod);
	}

	@Override
	public ResponseEntity<JobResponse> updateJob(String jobName, Date jobScheduleTime, String cronExpression,
			String endpoint) {
		logger.info("JobServiceImpl.scheduleJob()...");
		return schedulerUtil.updateJob(jobName, jobScheduleTime, cronExpression, endpoint);
	}

	@Override
	public boolean unScheduleJob(String jobName) {
		logger.info("JobServiceImpl.unScheduleJob()...");
		return schedulerUtil.unScheduleJob(jobName);
	}

	@Override
	public ResponseEntity<JobResponse> deleteJob(String jobName) {
		logger.info("JobServiceImpl.deleteJob()...");
		return schedulerUtil.deleteJob(jobName);
	}

	@Override
	public ResponseEntity<JobResponse> pauseJob(String jobName) {
		logger.info("JobServiceImpl.pauseJob()...");
		return schedulerUtil.pauseJob(jobName);
	}

	@Override
	public ResponseEntity<JobResponse> resumeJob(String jobName) {
		logger.info("JobServiceImpl.resumeJob()...");
		return schedulerUtil.resumeJob(jobName);
	}

	@Override
	public ResponseEntity<JobResponse> startJobNow(String jobName) {
		logger.info("JobServiceImpl.startJobNow()...");
		return schedulerUtil.startJobNow(jobName);
	}

	@Override
	public ResponseEntity<JobResponse> stopJob(String jobName) {
		logger.info("JobServiceImpl.stopJob()...");
		return schedulerUtil.stopJob(jobName);
	}

	@Override
	public boolean isJobRunning(String jobName) {
		logger.info("JobServiceImpl.isJobRunning()...");
		return schedulerUtil.isJobRunning(jobName);
	}

	@Override
	public List<Map<String, Object>> getAllJobs() {
		logger.info("JobServiceImpl.getAllJobs()...");
		return schedulerUtil.getAllJobs();
	}

	@Override
	public boolean isJobWithNamePresent(String jobName) {
		logger.info("JobServiceImpl.isJobWithNamePresent()...");
		return schedulerUtil.isJobWithNamePresent(jobName);
	}

	@Override
	public String getJobState(String jobName) {
		logger.info("JobServiceImpl.getJobState()...");
		return schedulerUtil.getJobState(jobName);
	}

	@Override
	public Map<String, Object> getJobDetails(String jobName) {
		logger.info("JobServiceImpl.getJobDetails()...");
		return schedulerUtil.getJobDetails(jobName);
	}

}
