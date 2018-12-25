package org.ril.hrss.scheduler.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import org.ril.hrss.msf.util.HRSSConstantUtil;
import org.ril.hrss.scheduler.listener.HrssJobListener;
import org.ril.hrss.scheduler.listener.HrssTriggerListner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzSchedulerConfig {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private HrssTriggerListner triggerListner;

	@Autowired
	private HrssJobListener jobsListener;

	@Value("${org.quartz.scheduler.instanceName}")
	public String instanceName;

	@Value("${org.quartz.scheduler.instanceId}")
	public String instanceId;

	@Value("${org.quartz.threadPool.threadCount}")
	public String threadCount;

	@Value("${org.quartz.scheduler.jobFactory.class}")
	public String jobFactoryClass;

	@Value("${org.quartz.threadPool.class}")
	public String threadPoolClass;

	@Value("${org.plugin.shutdownHook.class}")
	public String shutdownHookClass;

	@Value("${org.plugin.shutdownHook.cleanShutdown}")
	public String cleanShutdown;

	@Value("${org.quartz.jobStore.class}")
	public String jobStoreClass;

	@Value("${org.quartz.jobStore.driverDelegateClass}")
	public String driverDelegateClass;

	@Value("${org.quartz.jobStore.useProperties}")
	public String jobStoreProperties;

	@Value("${org.quartz.jobStore.misfireThreshold}")
	public String misfireThreshold;

	@Value("${org.quartz.jobStore.tablePrefix}")
	public String tablePrefix;

	@Value("${org.quartz.jobStore.isClustered}")
	public String isClustered;

	/**
	 * create scheduler
	 */
	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException {

		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setOverwriteExistingJobs(true);
		factory.setDataSource(dataSource);
		factory.setQuartzProperties(quartzProperties());

		// Register listeners to get notification on Trigger misfire etc
		factory.setGlobalTriggerListeners(triggerListner);
		factory.setGlobalJobListeners(jobsListener);

		AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		factory.setJobFactory(jobFactory);

		return factory;
	}

	/**
	 * Configure quartz using properties file
	 */
	@Bean
	public Properties quartzProperties() throws IOException {
		Properties quartzProperties = new Properties();
		quartzProperties.setProperty(HRSSConstantUtil.INSTANCE_NAME_KEY, instanceName);
		quartzProperties.setProperty(HRSSConstantUtil.INSTANCE_ID_KEY, instanceId);
		quartzProperties.setProperty(HRSSConstantUtil.THREAD_COUNT_KEY, threadCount);
		quartzProperties.setProperty(HRSSConstantUtil.JOB_FACTRY_KEY, jobFactoryClass);
		quartzProperties.setProperty(HRSSConstantUtil.JOBSTORE_CLS_KEY, jobStoreClass);
		quartzProperties.setProperty(HRSSConstantUtil.DRIVER_DELEGATE_KEY, driverDelegateClass);
		quartzProperties.setProperty(HRSSConstantUtil.JOBSTORE_PROP_KEY, jobStoreProperties);
		quartzProperties.setProperty(HRSSConstantUtil.MISFIRE_THRESHOLD_KEY, misfireThreshold);
		quartzProperties.setProperty(HRSSConstantUtil.TABLE_PREFIX_KEY, tablePrefix);
		quartzProperties.setProperty(HRSSConstantUtil.IS_CLUSTERED_KEY, isClustered);
		quartzProperties.setProperty(HRSSConstantUtil.THREADPOOL_CLS_KEY, threadPoolClass);
		quartzProperties.setProperty(HRSSConstantUtil.SHUTDOWN_HOOK_CLS_KEY, shutdownHookClass);
		quartzProperties.setProperty(HRSSConstantUtil.CLEAN_SHUTDOWN_KEY, cleanShutdown);

		return quartzProperties;
	}

}
