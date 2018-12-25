package org.ril.hrss.scheduler.listener;

import java.util.logging.Logger;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.springframework.stereotype.Component;

@Component
public class HrssJobListener implements JobListener {

	protected static final Logger logger = Logger.getLogger(HrssJobListener.class.getName());

	@Override
	public String getName() {
		return HRSSConstantUtil.GLOBAL_JOB_KEY;
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		logger.info("HrssJobListener.jobToBeExecuted()..");
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		logger.info("HrssJobListener.jobExecutionVetoed()..");
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		logger.info("HrssJobListener.jobWasExecuted()..");
	}

}
