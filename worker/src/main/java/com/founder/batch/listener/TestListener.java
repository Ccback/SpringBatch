package com.founder.batch.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class TestListener implements JobExecutionListener {
    private static Log log = LogFactory.getLog(JobExecutionListener.class);
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("beforeJob------------------jobExecution："+jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if( jobExecution.getStatus() == BatchStatus.COMPLETED ){
            log.info("afterJob------------------------COMPLETED");
        }
        else if(jobExecution.getStatus() == BatchStatus.FAILED){
            log.info("afterJob------------------------FAILED");
        }
    }
}
