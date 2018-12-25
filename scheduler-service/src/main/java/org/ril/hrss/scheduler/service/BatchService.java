package org.ril.hrss.scheduler.service;

import org.ril.hrss.scheduler.kafka.model.BatchDetailsMsg;

public interface BatchService {

	public BatchDetailsMsg updateBatchDetails(String batchUuid, String status);

}
