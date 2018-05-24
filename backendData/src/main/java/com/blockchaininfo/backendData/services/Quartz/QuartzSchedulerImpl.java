package com.blockchaininfo.backendData.services.Quartz;

import com.blockchaininfo.backendData.repositories.NetworkHashrateRepository;
import com.blockchaininfo.backendData.repositories.PoolHashrateRepository;
import com.blockchaininfo.backendData.services.GetDataService;
import com.blockchaininfo.backendData.services.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Profile("quartz")
@Slf4j
@Service
public class QuartzSchedulerImpl extends SchedulerService implements ApplicationListener<ContextRefreshedEvent> {

    public QuartzSchedulerImpl(NetworkHashrateRepository networkHashrateRepository, PoolHashrateRepository poolHashrateRepository, GetDataService getDataService) {
        super(networkHashrateRepository, poolHashrateRepository, getDataService);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent){

        this.runScheduledTask();
    }

    public void runScheduledTask(){

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            JobDetail job = JobBuilder.newJob(GatherDataJob.class).build();
            job.getJobDataMap().put("getDataService", getDataService);

            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(5).repeatForever())
                    .build();

            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException e){
            log.info("" + e);
        }
    }
}
