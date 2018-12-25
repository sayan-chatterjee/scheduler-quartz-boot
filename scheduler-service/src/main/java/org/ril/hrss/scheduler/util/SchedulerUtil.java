package org.ril.hrss.scheduler.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.job.CronJob;
import org.ril.hrss.scheduler.job.SimpleJob;
import org.ril.hrss.scheduler.model.JobResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class SchedulerUtil {

	protected static final Logger logger = Logger.getLogger(SchedulerUtil.class.getName());

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
			return jobUtil.getJobResponse(HRSSConstantUtil.GENERIC_ERROR_KEY, HRSSConstantUtil.GENERIC_ERROR_MSG);
		}
	}

	/**
	 * Schedule a one time simple job by jobName at given date.
	 */
	private boolean scheduleOneTimeJob(String jobName, String endpoint, Class<? extends QuartzJobBean> jobClass,
			Date date, String payload, String httpMethod) {
		logger.info("Request received to scheduleOneTimeJo");

		String jobKey = jobName;
		String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
		String triggerKey = jobName;

		JobDetail jobDetail = jobUtil.createJob(jobClass, false, context, jobKey, groupKey, httpMethod, endpoint,
				payload);

		logger.info("creating trigger for key simple job");
		Trigger cronTriggerBean = jobUtil.createSingleTrigger(triggerKey, date,
				SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			scheduler.scheduleJob(jobDetail, cronTriggerBean);
			logger.info("Simple Job with key created");
			return true;
		} catch (SchedulerException e) {
			logger.info("SchedulerException while scheduling job JobServiceImpl.scheduleOneTimeJob()");
		}

		return false;
	}

	/**
	 * Schedule a cron job by jobName at given cron.
	 */
	private boolean scheduleCronJob(String jobName, String endpoint, Class<? extends QuartzJobBean> jobClass, Date date,
			String cronExpression, String payload, String httpMethod) {
		logger.info("Request received to scheduleCronJob");

		String jobKey = jobName;
		String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
		String triggerKey = jobName;

		JobDetail jobDetail = jobUtil.createJob(jobClass, false, context, jobKey, groupKey, httpMethod, endpoint,
				payload);

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
	public ResponseEntity<JobResponse> scheduleJob(String jobName, String endpoint, Date jobScheduleTime,
			String cronExpression, String payload, String httpMethod) {
		payload = StringUtils.isEmpty(payload) ? null : payload;
		// Check if job Name is unique;
		if (!this.isJobWithNamePresent(jobName)) {
			if (StringUtils.isEmpty(cronExpression)) {
				// Single Trigger
				boolean status = this.scheduleOneTimeJob(jobName, endpoint, SimpleJob.class, jobScheduleTime, payload,
						httpMethod);
				return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY, this.getAllJobs());
			} else {
				// Cron Trigger
				boolean status = this.scheduleCronJob(jobName, endpoint, CronJob.class, jobScheduleTime,
						cronExpression.trim(), payload, httpMethod);
				return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY, this.getAllJobs());
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
			Trigger newTrigger = jobUtil.createSingleTrigger(jobKey, date, SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

			schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger);
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
	private boolean updateCronJob(String jobName, Date date, String cronExpression, String endpoint) {
		logger.info("Request received for updating cron job.");
		String jobKey = jobName;
		String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
		try {
			Trigger newTrigger = jobUtil.createCronTrigger(jobKey, date, cronExpression,
					SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

			if (StringUtils.isNotEmpty(endpoint) && !this.isJobRunning(jobName)) {
				JobKey jkey = new JobKey(jobKey, groupKey);
				JobDetail jobDetail = schedulerFactoryBean.getScheduler().getJobDetail(jkey);
				jobDetail.getJobDataMap().put(HRSSConstantUtil.SERVICE_ENDPOINT, endpoint);
				this.delete(jobName);
				schedulerFactoryBean.getScheduler().scheduleJob(jobDetail, newTrigger);
			} else {
				schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobKey), newTrigger);
			}

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
	public ResponseEntity<JobResponse> updateJob(String jobName, Date jobScheduleTime, String cronExpression,
			String endpoint) {
		// Edit Job
		if (this.isJobWithNamePresent(jobName)) {

			if (StringUtils.isNotEmpty(cronExpression.trim())) {

				// Cron Trigger
				boolean status = this.updateCronJob(jobName, jobScheduleTime, cronExpression, endpoint);
				return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY, this.getJobDetails(jobName));
			} else {
				// Single Trigger
				boolean status = this.updateOneTimeJob(jobName, jobScheduleTime);
				return this.processResponse(status, HRSSConstantUtil.SUCCESS_KEY, this.getJobDetails(jobName));
			}

		} else {
			return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY, HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
		}
	}

	/**
	 * Remove the indicated Trigger from the scheduler. If the related job does not
	 * have any other triggers, and the job is not durable, then the job will also
	 * be deleted.
	 */
	public boolean unScheduleJob(String jobName) {
		logger.info("Request received for Unscheduleding job.");

		String jobKey = jobName;
		TriggerKey tkey = new TriggerKey(jobKey);

		try {
			return schedulerFactoryBean.getScheduler().unscheduleJob(tkey);
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
			return schedulerFactoryBean.getScheduler().deleteJob(jkey);
		} catch (SchedulerException e) {
			logger.info("SchedulerException while deleting job");
			return false;
		}
	}

	/**
	 * Delete the identified Job from the Scheduler - and any associated Triggers.
	 */
	public ResponseEntity<JobResponse> deleteJob(String jobName) {
		if (this.isJobWithNamePresent(jobName)) {
			if (!this.isJobRunning(jobName)) {
				return this.processResponse(this.delete(jobName), HRSSConstantUtil.SUCCESS_KEY,
						HRSSConstantUtil.SUCCESS_MSG);
			} else {
				return jobUtil.getJobResponse(HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_KEY,
						HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_MSG);
			}
		} else {
			// Job doesn't exist
			return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY, HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
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
	public ResponseEntity<JobResponse> pauseJob(String jobName) {
		if (this.isJobWithNamePresent(jobName)) {
			if (!this.isJobRunning(jobName)) {
				return this.processResponse(this.pause(jobName), HRSSConstantUtil.SUCCESS_KEY,
						HRSSConstantUtil.SUCCESS_MSG);
			} else {
				return jobUtil.getJobResponse(HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_KEY,
						HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_MSG);
			}

		} else {
			// Job doesn't exist
			return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY, HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
		}
	}

	private boolean resume(String jobName) {
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
	public ResponseEntity<JobResponse> resumeJob(String jobName) {
		if (this.isJobWithNamePresent(jobName)) {
			if (HRSSConstantUtil.JOB_STATE_PAUSED.equals(this.getJobState(jobName))) {
				logger.info("Job current state is PAUSED, Resuming job...");
				return this.processResponse(this.resume(jobName), HRSSConstantUtil.SUCCESS_KEY,
						HRSSConstantUtil.SUCCESS_MSG);
			} else {
				return jobUtil.getJobResponse(HRSSConstantUtil.JOB_NOT_IN_PAUSED_STATE_KEY,
						HRSSConstantUtil.JOB_NOT_IN_PAUSED_STATE_MSG);
			}

		} else {
			// Job doesn't exist
			return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY, HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
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
	public ResponseEntity<JobResponse> startJobNow(String jobName) {
		if (this.isJobWithNamePresent(jobName)) {
			if (!this.isJobRunning(jobName)) {
				return this.processResponse(this.startJobImmediate(jobName), HRSSConstantUtil.SUCCESS_KEY,
						HRSSConstantUtil.SUCCESS_MSG);

			} else {
				// Job already running
				return jobUtil.getJobResponse(HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_KEY,
						HRSSConstantUtil.JOB_ALREADY_IN_RUNNING_STATE_MSG);
			}

		} else {
			// Job doesn't exist
			return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY, HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
		}
	}

	/**
	 * Check if job is already running
	 */
	public boolean isJobRunning(String jobName) {
		logger.info("Request received to check if job is running");
		try {
			List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
			return currentJobs.stream().anyMatch(jobCtx -> 
				jobName.equalsIgnoreCase(jobCtx.getJobDetail().getKey().getName()) && 
						HRSSConstantUtil.JOB_SAMPLE_GRP.equalsIgnoreCase(jobCtx.getJobDetail().getKey().getGroup())
			);

		} catch (SchedulerException e) {
			logger.info("SchedulerException while checking if job is running");
		}
		return false;
	}

	/**
	 * Get all jobs
	 */
	public List<Map<String, Object>> getAllJobs() {
		List<Map<String, Object>> list = new ArrayList<>();
		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();

			scheduler.getJobGroupNames().stream().forEach(groupName -> {
				try {
					scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).stream().forEach(jobKey -> {
						String jobName = jobKey.getName();
						String jobGroup = jobKey.getGroup();
						try {
							// get job's trigger
							List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);

							Date scheduleTime = triggers.get(HRSSConstantUtil.ZERO.intValue()).getStartTime();
							Date nextFireTime = triggers.get(HRSSConstantUtil.ZERO.intValue()).getNextFireTime();
							Date lastFiredTime = triggers.get(HRSSConstantUtil.ZERO.intValue()).getPreviousFireTime();

							Map<String, Object> map = new HashMap<>();
							map.put(HRSSConstantUtil.JOB_NAME, jobName);
							map.put(HRSSConstantUtil.JOB_GRP_NAME, jobGroup);
							map.put(HRSSConstantUtil.JOB_SCHDLD_TIME, scheduleTime);
							map.put(HRSSConstantUtil.JOB_LASTFIRE_TIME, lastFiredTime);
							map.put(HRSSConstantUtil.JOB_NXTFIRE_TIME, nextFireTime);

							JobDataMap jobDataMap = scheduler.getJobDetail(jobKey).getJobDataMap();
							map.put(HRSSConstantUtil.SERVICE_ENDPOINT,
									jobDataMap.getString(HRSSConstantUtil.SERVICE_ENDPOINT));
							map.put(HRSSConstantUtil.SERVICE_METHOD,
									jobDataMap.getString(HRSSConstantUtil.SERVICE_METHOD));
							if (isJobRunning(jobName)) {
								map.put(HRSSConstantUtil.JOB_STATUS, HRSSConstantUtil.JOB_STATE_RUNNING);
							} else {
								map.put(HRSSConstantUtil.JOB_STATUS, getJobState(jobName));
							}

							list.add(map);
						} catch (SchedulerException e) {
							logger.info("SchedulerException while fetching all jobs.");
						}
					});
				} catch (SchedulerException e) {
					logger.info("SchedulerException while fetching all jobs.");
				}
			});
		} catch (SchedulerException e) {
			logger.info("SchedulerException while fetching all jobs.");
		}
		return list;
	}

	/**
	 * Check job exist with given name
	 */
	public boolean isJobWithNamePresent(String jobName) {
		JobKey jobKey = new JobKey(jobName, HRSSConstantUtil.JOB_SAMPLE_GRP);
		Scheduler scheduler = schedulerFactoryBean.getScheduler();
		try {
			return scheduler.checkExists(jobKey);
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
		String result = null;
		try {
			String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
			JobKey jobKey = new JobKey(jobName, groupKey);

			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);

			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
			result = scheduler.getTriggerState(triggers.stream().findFirst().get().getKey()).name();

		} catch (SchedulerException e) {
			logger.info("SchedulerException while checking job state");
		}
		return HRSSConstantUtil.JOB_STATE_NORMAL.equals(result) ? HRSSConstantUtil.JOB_STATE_SCHEDULED : result;
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
	public ResponseEntity<JobResponse> stopJob(String jobName) {
		logger.info("JobServiceImpl.stopJob()");
		if (this.isJobWithNamePresent(jobName)) {
			if (this.isJobRunning(jobName)) {
				return this.processResponse(this.stop(jobName), HRSSConstantUtil.SUCCESS_KEY,
						HRSSConstantUtil.SUCCESS_MSG);
			} else {
				// Job not in running state
				return jobUtil.getJobResponse(HRSSConstantUtil.JOB_NOT_IN_RUNNING_STATE_KEY,
						HRSSConstantUtil.JOB_NOT_IN_RUNNING_STATE_MSG);
			}

		} else {
			// Job doesn't exist
			return jobUtil.getJobResponse(HRSSConstantUtil.JOB_DOESNT_EXIST_KEY, HRSSConstantUtil.JOB_DOESNT_EXIST_MSG);
		}
	}

	public Map<String, Object> getJobDetails(String jobName) {
		Map<String, Object> map = new HashMap<>();
		if (this.isJobWithNamePresent(jobName)) {
			String groupKey = HRSSConstantUtil.JOB_SAMPLE_GRP;
			JobKey jobKey = new JobKey(jobName, groupKey);
			Scheduler scheduler = schedulerFactoryBean.getScheduler();

			try {
				List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
				Date scheduleTime = triggers.get(HRSSConstantUtil.ZERO.intValue()).getStartTime();
				Date nextFireTime = triggers.get(HRSSConstantUtil.ZERO.intValue()).getNextFireTime();
				Date lastFiredTime = triggers.get(HRSSConstantUtil.ZERO.intValue()).getPreviousFireTime();

				map.put(HRSSConstantUtil.JOB_NAME, jobName);
				map.put(HRSSConstantUtil.JOB_GRP_NAME, groupKey);
				map.put(HRSSConstantUtil.JOB_SCHDLD_TIME, scheduleTime);
				map.put(HRSSConstantUtil.JOB_LASTFIRE_TIME, lastFiredTime);
				map.put(HRSSConstantUtil.JOB_NXTFIRE_TIME, nextFireTime);

				JobDetail jobDetail = scheduler.getJobDetail(jobKey);
				JobDataMap jobDataMap = jobDetail.getJobDataMap();
				map.put(HRSSConstantUtil.SERVICE_ENDPOINT, jobDataMap.getString(HRSSConstantUtil.SERVICE_ENDPOINT));
				map.put(HRSSConstantUtil.SERVICE_METHOD, jobDataMap.getString(HRSSConstantUtil.SERVICE_METHOD));

				if (StringUtils.isNotEmpty(jobDataMap.getString(HRSSConstantUtil.SERVICE_PAYLOAD))) {
					JSONObject payloadJson = new JSONObject(jobDataMap.getString(HRSSConstantUtil.SERVICE_PAYLOAD));
					String payload = payloadJson.toString();
					map.put(HRSSConstantUtil.SERVICE_PAYLOAD,
							payload.replaceAll(HRSSConstantUtil.DOUBLE_QUOTE, HRSSConstantUtil.SINGLE_QUOTE));
				}
				if (isJobRunning(jobName)) {
					map.put(HRSSConstantUtil.JOB_STATUS, HRSSConstantUtil.JOB_STATE_RUNNING);
				} else {
					map.put(HRSSConstantUtil.JOB_STATUS, getJobState(jobName));
				}

			} catch (SchedulerException e) {
				logger.info("SchedulerException while getting job details.");
			} catch (JSONException e) {
				logger.info("JSONException while getting job details.");
			}

		}
		return map;
	}

}
