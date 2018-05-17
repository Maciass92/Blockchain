package com.example.blockchaininfo.services.Quartz;

import com.example.blockchaininfo.services.GetDataService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("quartz")
@Component
public class GatherDataJob extends QuartzJobBean {

    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) {

        GetDataService getDataService = (GetDataService) jobExecutionContext.getJobDetail().getJobDataMap().get("getDataService");

        getDataService.storeData();
    }
}
