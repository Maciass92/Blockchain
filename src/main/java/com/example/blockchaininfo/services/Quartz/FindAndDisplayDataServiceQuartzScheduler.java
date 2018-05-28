package com.example.blockchaininfo.services.Quartz;

import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.example.blockchaininfo.services.FindAndDisplayDataService;
import com.example.blockchaininfo.services.GetDataService;
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
public class FindAndDisplayDataServiceQuartzScheduler extends FindAndDisplayDataService implements ApplicationListener<ContextRefreshedEvent>  {

    public FindAndDisplayDataServiceQuartzScheduler(NetworkHashrateRepository networkHashrateRepository, PoolHashrateRepository poolHashrateRepository, GetDataService getDataService) {
        super(networkHashrateRepository, poolHashrateRepository, getDataService);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent){

        this.runScheduledTask();
    }

    @Override
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
