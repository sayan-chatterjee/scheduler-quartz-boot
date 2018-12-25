package org.ril.hrss.scheduler.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "BATCH_DETAILS")
public class BatchDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer batchId;

	@NotNull
	private String jobName;

	@NotNull
	private String endpoint;

	@NotNull
	private String batchStatus;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	private Date endTime;

	@NotNull
	@Column(name = "BATCH_UUID", unique = true)
	private String batchUuid;

	public BatchDetails() {
		super();
	}

	public BatchDetails(Integer batchId, String jobName, String endpoint, String batchStatus, Date startTime,
			Date endTime, String batchUuid) {
		this();
		this.batchId = batchId;
		this.jobName = jobName;
		this.endpoint = endpoint;
		this.batchStatus = batchStatus;
		this.startTime = startTime;
		this.endTime = endTime;
		this.batchUuid = batchUuid;
	}

	public Integer getBatchId() {
		return batchId;
	}

	public void setBatchId(Integer batchId) {
		this.batchId = batchId;
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

}
