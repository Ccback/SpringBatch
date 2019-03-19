package com.founder.batch.config.quartz;

import com.founder.batch.jobLauncher.DataBaseJobLauncher;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SimpleQuartzConfiguration {
    //自动注入进来的是SimpleJobLauncher
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobLocator jobLocator;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*用来注册job*/
    /*JobRegistry会自动注入进来*/
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry){
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    @Bean("JobDetailFactoryBean1")
    public JobDetailFactoryBean jobDetailFactoryBean(){
        JobDetailFactoryBean jobFactory = new JobDetailFactoryBean();
        jobFactory.setJobClass(DataBaseJobLauncher.class);
        DataBaseJobLauncher.setJobLauncher(jobLauncher);
        DataBaseJobLauncher.setJobLocator(jobLocator);
        DataBaseJobLauncher.setJdbcTemplate(jdbcTemplate);
        jobFactory.setGroup("simple_group");
        jobFactory.setName("simple_job");
        Map<String, Object> map = new HashMap<>();
//        map.put("jobName", "footballJob");
//        map.put("jobLauncher", jobLauncher);
//        map.put("jobLocator", jobLocator);
        jobFactory.setJobDataAsMap(map);
        return jobFactory;
    }

    @Bean("CronTriggerFactoryBean1")
    public CronTriggerFactoryBean cronTriggerFactoryBean(){
        CronTriggerFactoryBean cTrigger = new CronTriggerFactoryBean();
        System.out.println("------- : " + jobDetailFactoryBean().getObject());
        cTrigger.setJobDetail(jobDetailFactoryBean().getObject());
        cTrigger.setStartDelay(5000);
        cTrigger.setName("simple_trigger");
        cTrigger.setGroup("simple_trigger_group");
        cTrigger.setCronExpression("0/20 * * * * ? "); //每间隔5s触发一次Job任务
        return cTrigger;
    }
    @Bean("SchedulerFactoryBean1")
    public SchedulerFactoryBean schedulerFactoryBean(){
        SchedulerFactoryBean schedulerFactor = new SchedulerFactoryBean();
        schedulerFactor.setDataSource(dataSource);
        schedulerFactor.setTriggers(cronTriggerFactoryBean().getObject());
        return schedulerFactor;
    }
}
