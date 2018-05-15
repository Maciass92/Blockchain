package com.example.blockchaininfo.services.Quartz;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.example.blockchaininfo.services.FindAndDisplayDataService;
import com.example.blockchaininfo.services.GetDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Profile("quartz")
@Slf4j
@Service
public class FindAndDisplayDataServiceQuartz implements ApplicationListener<ContextRefreshedEvent>, FindAndDisplayDataService {

    private final NetworkHashrateRepository networkHashrateRepository;
    private final PoolHashrateRepository poolHashrateRepository;
    private final GetDataService getDataService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent){

        this.startQuartzScheduling();
    }

    public void startQuartzScheduling (){

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            JobDetail job = JobBuilder.newJob(SampleJob.class).build();
            job.getJobDataMap().put("getDataService", this.getDataService);

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

    public List<NetworkHashrate> getAllNetworks(){

        return networkHashrateRepository.findAll();
    }

    public List<PoolHashrate> getAllPools(Long id){

        return poolHashrateRepository.findAllByNetworkId(id);
    }

    public String returnNetworkAsJson() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(this.getAllNetworks());
    }
}
