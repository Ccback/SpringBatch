package com.founder.batch.jobLauncher;


import org.quartz.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseJobLauncher extends QuartzJobBean {
    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public static void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        DataBaseJobLauncher.jdbcTemplate = jdbcTemplate;
    }

    private static JdbcTemplate jdbcTemplate;

    private static JobLauncher jobLauncher;


    private static JobLocator jobLocator;

    private static RunIdIncrementer runIdIncrementer = new RunIdIncrementer();

    public static JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    public static void setJobLauncher(JobLauncher jobLauncher) {
        DataBaseJobLauncher.jobLauncher = jobLauncher;
    }

    public static JobLocator getJobLocator() {
        return jobLocator;
    }

    public static void setJobLocator(JobLocator jobLocator) {
        DataBaseJobLauncher.jobLocator = jobLocator;
    }

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        String jobName = jobDataMap.getString("jobName");
//        JobLauncher jobLauncher = (JobLauncher) jobDataMap.get("jobLauncher");
//        JobLocator jobLocator = (JobLocator) jobDataMap.get("jobLocator");
        System.out.println("jobName : " + jobName);
        System.out.println("jobLauncher : " + jobLauncher);
        System.out.println("jobLocator : " + jobLocator);
        JobKey key = jobExecutionContext.getJobDetail().getKey();
        System.out.println(key.getName() + " : " + key.getGroup());

        List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from FCR_JOB_CONTROL where err_code = '0'");
        for(Map<String, Object> FcrJob:list){
            String fcrJobName = FcrJob.get("FCR_JOBNAME").toString();
            try {
                Job job = jobLocator.getJob(fcrJobName);
                /*启动spring batch的job*/
//            JobParameters jobParameters = runIdIncrementer.getNext(null);
//            System.out.println(jobParameters);
                Map<String, JobParameter> map = new HashMap<>();
                map.put("data",new JobParameter(new Date()));
                JobExecution jobExecution = jobLauncher.run(job, new JobParameters(map));
                System.out.println(jobExecution);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(list);
    }
}
