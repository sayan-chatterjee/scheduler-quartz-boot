package org.ril.hrss.scheduler.repository;

import org.ril.hrss.scheduler.entity.BatchDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BatchDetailsRepository extends JpaRepository<BatchDetails, Integer> {

	@Query("select batchDetails from BatchDetails batchDetails where batchDetails.batchUuid=:batchUuid")
	public BatchDetails findByBatchUuid(@Param("batchUuid") String batchUuid);
}
