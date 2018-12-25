package org.ril.hrss.scheduler.kafka.model;

import java.io.Serializable;
import java.util.Date;

public class BatchDetailsMsg implements Serializable {

	private static final long serialVersionUID = 1L;

	private String jobName;

	private String endpoint;

	private String batchStatus;

	private Date startTime;

	private Date endTime;

	private String batchUuid;

	public BatchDetailsMsg() {
		super();
	}

	public BatchDetailsMsg(String jobName, String endpoint, String batchStatus, Date startTime, Date endTime,
			String batchUuid) {
		this();
		this.jobName = jobName;
		this.endpoint = endpoint;
		this.batchStatus = batchStatus;
		this.startTime = startTime;
		this.endTime = endTime;
		this.batchUuid = batchUuid;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getBatchStatus() {
		return batchStatus;
	}

	public void setBatchStatus(String batchStatus) {
		this.batchStatus = batchStatus;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getBatchUuid() {
		return batchUuid;
	}

	public void setBatchUuid(String batchUuid) {
		this.batchUuid = batchUuid;
	}

	@Override
	public String toString() {
		return "BatchDetailsMsg [jobName=" + jobName + ", endpoint=" + endpoint + ", batchStatus=" + batchStatus
				+ ", startTime=" + startTime + ", endTime=" + endTime + ", batchUuid=" + batchUuid + "]";
	}

}
