package org.ril.hrss.scheduler.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import org.ril.hrss.msf.util.DateUtil;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.entity.BatchDetails;
import org.ril.hrss.scheduler.kafka.model.BatchDetailsMsg;
import org.ril.hrss.scheduler.repository.BatchDetailsRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BatchServiceImpl implements BatchService {

	protected static final Logger logger = Logger.getLogger(BatchServiceImpl.class.getName());

	@Autowired
	private BatchDetailsRepository batchDetailsRepository;

	@Override
	public BatchDetailsMsg updateBatchDetails(String batchUuid, String status) {
		logger.info("BatchServiceImpl.updateBatchDetails()");
		BatchDetails batchDetails = batchDetailsRepository.findByBatchUuid(batchUuid);
		batchDetails.setBatchStatus(status);
		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_TIME_PATTERN);
		try {
			batchDetails.setEndTime(sdf.parse(DateUtil
					.getSimpleFormatDate(DateUtil.getDate(HRSSConstantUtil.EMPTY_STRING), DateUtil.DATE_TIME_PATTERN)));
		} catch (ParseException e) {
			logger.info(HRSSConstantUtil.UNEXPECTED_ERROR_OCCURRED);
		}
		BatchDetailsMsg batchDetailsMsg = new BatchDetailsMsg();
		BeanUtils.copyProperties(batchDetailsRepository.save(batchDetails), batchDetailsMsg);
		return batchDetailsMsg;
	}

}
