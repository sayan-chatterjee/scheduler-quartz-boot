package org.ril.hrss.scheduler.job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.ril.hrss.msf.util.DateUtil;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.entity.BatchDetails;
import org.ril.hrss.scheduler.repository.BatchDetailsRepository;
import org.ril.hrss.scheduler.util.JobUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class CronJob extends QuartzJobBean implements InterruptableJob {

	protected static final Logger logger = Logger.getLogger(CronJob.class.getName());

	@Autowired
	private JobUtil jobUtil;

	@Autowired
	private BatchDetailsRepository batchDetailsRepository;

	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		logger.info("========== Cron Job started ==========");
		/*********** For retrieving stored key-value pairs ***********/
		JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();

		String endpoint = dataMap.getString(HRSSConstantUtil.SERVICE_ENDPOINT);
		String jobName = jobExecutionContext.getJobDetail().getKey().getName();
		String method = dataMap.getString(HRSSConstantUtil.SERVICE_METHOD);
		String payload = dataMap.getString(HRSSConstantUtil.SERVICE_PAYLOAD);

		SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATE_TIME_PATTERN);
		BatchDetails batchdetails = null;
		try {
			batchdetails = new BatchDetails(null, jobName, endpoint, HRSSConstantUtil.BATCH_STATUS_INPROGRESS,
					sdf.parse(DateUtil.getSimpleFormatDate(DateUtil.getDate(HRSSConstantUtil.EMPTY_STRING),
							DateUtil.DATE_TIME_PATTERN)),
					null, UUID.randomUUID().toString());
			batchdetails = batchDetailsRepository.save(batchdetails);
		} catch (ParseException e) {
			logger.info(HRSSConstantUtil.UNEXPECTED_ERROR_OCCURRED);
		}
		List<NameValuePair> nameValuePairs = new ArrayList<>();
		nameValuePairs.add(new BasicNameValuePair(HRSSConstantUtil.QRY_PARAM_BATCH_UUID, batchdetails.getBatchUuid()));

		switch (method) {
			case HRSSConstantUtil.HTTP_GET_REQUEST:
				jobUtil.callGETEndpoint(jobUtil.createURL(endpoint, nameValuePairs));
				break;
			case HRSSConstantUtil.HTTP_POST_REQUEST:
				jobUtil.callPOSTEndpoint(jobUtil.createURL(endpoint, nameValuePairs), payload);
				break;
			case HRSSConstantUtil.HTTP_PUT_REQUEST:
				jobUtil.callPUTEndpoint(jobUtil.createURL(endpoint, nameValuePairs), payload);
				break;
			case HRSSConstantUtil.HTTP_DELETE_REQUEST:
				jobUtil.callDELETEEndpoint(jobUtil.createURL(endpoint, nameValuePairs));
				break;
		}
		logger.info("========== Cron Job ended with key ==========");
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		logger.info("Stopping job... ");
	}

}
