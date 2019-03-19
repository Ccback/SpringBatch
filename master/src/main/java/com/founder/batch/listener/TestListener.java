package com.founder.batch.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestListener implements JobExecutionListener {
    private static Log log = LogFactory.getLog(JobExecutionListener.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("beforeJob------------------jobExecution："+jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if( jobExecution.getStatus() == BatchStatus.COMPLETED ){
            log.info("afterJob------------------------COMPLETED"+jobExecution.getJobInstance().getJobName());
            jdbcTemplate.update("update FCR_JOB_CONTROL set err_code = '0000' where FCR_jobname = ?",jobExecution.getJobInstance().getJobName());
        }
        else if(jobExecution.getStatus() == BatchStatus.FAILED){
            log.info("afterJob------------------------FAILED");
            jdbcTemplate.update("update FCR_JOB_CONTROL set err_code = '0001' where FCR_jobname = ?",jobExecution.getJobInstance().getJobName());
        }
    }
}
