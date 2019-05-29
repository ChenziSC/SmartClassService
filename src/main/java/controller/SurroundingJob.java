package controller;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class SurroundingJob implements Job  {

    @Autowired
    private QuartzManagerSurroundings quartzManagerSurrounding;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        quartzManagerSurrounding.addSurroudings();
    }
}
