package com.example.blockchaininfo.services.Quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("quartz")
@Component
public class SampleJob extends QuartzJobBean {

    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) {

        GetDataServiceQuartz getDataServiceQuartz = (GetDataServiceQuartz) jobExecutionContext.getJobDetail().getJobDataMap().get("getDataServiceQuartz");

        getDataServiceQuartz.storeData();
    }
}
