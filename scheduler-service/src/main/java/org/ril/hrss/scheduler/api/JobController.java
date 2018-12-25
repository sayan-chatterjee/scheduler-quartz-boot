package org.ril.hrss.scheduler.api;

import java.util.Date;
import java.util.logging.Logger;

import javax.validation.constraints.NotNull;

import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.model.JobResponse;
import org.ril.hrss.scheduler.service.JobService;
import org.ril.hrss.scheduler.util.JobUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class JobController {

	protected static final Logger logger = Logger.getLogger(JobController.class.getName());

	@Autowired
	@Lazy
	private JobService jobService;

	@Autowired
	private JobUtil jobUtil;

	@RequestMapping(value = "/schedule", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Create cron or one time schedule job with jobData", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully saved schedule job request"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> schedule(@NotNull @RequestHeader("jobName") String jobName,
			@NotNull @RequestHeader("endpoint") String endpoint,
			@ApiParam(value = "HTTP method values") @NotNull @RequestHeader(required = true, value = "method") HttpMethod method,
			@ApiParam(value = "one time job schedule in format yyyy/MM/dd HH:mm") @RequestHeader(required = false, value = "jobScheduleTime") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date jobScheduleTime,
			@ApiParam(value = "cron expression e.g. 0 X/2 X ? X X means run every two minutes; replace X by Asterisk (star)") @RequestHeader(required = false, value = "cronExpression") String cronExpression,
			@RequestBody(required = false) String payload) {
		logger.info("JobController.schedule()");
		return jobService.scheduleJob(jobName, endpoint, jobScheduleTime, cronExpression, payload, method.name());
	}

	@RequestMapping(value = "/unschedule", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Unschedule Object", response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully unscheduled request"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public void unschedule(@NotNull @RequestHeader("jobName") String jobName) {
		logger.info("JobController.unschedule()");
		jobService.unScheduleJob(jobName);
	}

	@RequestMapping(value = "/delete", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Delete schedule Object", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully deleted job"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> delete(@NotNull @RequestHeader("jobName") String jobName) {
		logger.info("JobController.delete()");
		return jobService.deleteJob(jobName);
	}

	@RequestMapping(value = "/halt", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Pause schedule Object", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully paused job"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> halt(@NotNull @RequestHeader("jobName") String jobName) {
		logger.info("JobController.pause()");
		return jobService.pauseJob(jobName);
	}

	@RequestMapping(value = "/recoup", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Resume schedule Object", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully resumed job"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> resume(@NotNull @RequestHeader("jobName") String jobName) {
		logger.info("JobController.resume()");
		return jobService.resumeJob(jobName);
	}

	@RequestMapping(value = "/update", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Update schedule Object", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully updated job"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> updateJob(@NotNull @RequestHeader("jobName") String jobName,
			@RequestHeader(required = false, value = "jobScheduleTime") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm") Date jobScheduleTime,
			@RequestHeader(required = false, value = "cronExpression") String cronExpression,
			@RequestHeader("endpoint") String endpoint) {
		logger.info("JobController.updateJob()");
		return jobService.updateJob(jobName, jobScheduleTime, cronExpression, endpoint);
	}

	@RequestMapping(value = "/jobs", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get all schedule Objects", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully received all jobs"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> getAllJobs() {
		logger.info("JobController.getAllJobs()");
		return jobUtil.getJobResponse(HRSSConstantUtil.SUCCESS_KEY, jobService.getAllJobs());
	}

	@RequestMapping(value = "/jobs/{jobName}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get jobs details", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully received jobs details"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> getJobDetails(@NotNull @PathVariable("jobName") String jobName) {
		logger.info("JobController.getAllJobs()");
		return jobUtil.getJobResponse(HRSSConstantUtil.SUCCESS_KEY, jobService.getJobDetails(jobName));
	}

	@RequestMapping(value = "/checkJobName/{jobName}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Check Job Name", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully checked job name"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> checkJobName(@NotNull @PathVariable("jobName") String jobName) {
		logger.info("JobController.checkJobName()");
		return jobUtil.getJobResponse(HRSSConstantUtil.SUCCESS_KEY, jobService.isJobWithNamePresent(jobName));
	}

	@RequestMapping(value = "/isJobRunning/{jobName}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Check Job Is Running Or Not", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully received running status"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> isJobRunning(@NotNull @PathVariable("jobName") String jobName) {
		logger.info("JobController.isJobRunning()");
		return jobUtil.getJobResponse(HRSSConstantUtil.SUCCESS_KEY, jobService.isJobRunning(jobName));
	}

	@RequestMapping(value = "/jobState/{jobName}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get Job State", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully received job state"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> getJobState(@NotNull @PathVariable("jobName") String jobName) {
		logger.info("JobController.getJobState()");
		return jobUtil.getJobResponse(HRSSConstantUtil.SUCCESS_KEY, jobService.getJobState(jobName));
	}

	@RequestMapping(value = "/stop", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Stop schedule Object", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully stopped job"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> stopJob(@NotNull @RequestHeader("jobName") String jobName) {
		logger.info("JobController.stopJob()");
		return jobService.stopJob(jobName);
	}

	@RequestMapping(value = "/start", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Start schedule Job Object", response = JobResponse.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully processed request"),
			@ApiResponse(code = 201, message = "Successfully started job"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found") })
	public ResponseEntity<JobResponse> startJobNow(@NotNull @RequestHeader("jobName") String jobName) {
		logger.info("JobController.startJobNow()");
		return jobService.startJobNow(jobName);
	}

}
