package com.blockchaininfo.backendData.services.SpringScheduler;

import com.blockchaininfo.backendData.repositories.NetworkHashrateRepository;
import com.blockchaininfo.backendData.repositories.PoolHashrateRepository;
import com.blockchaininfo.backendData.services.GetDataService;
import com.blockchaininfo.backendData.services.SchedulerService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Profile("springScheduler")
@Service
@EnableScheduling
public class SpringSchedulerImpl extends SchedulerService {

    public SpringSchedulerImpl(NetworkHashrateRepository networkHashrateRepository, PoolHashrateRepository poolHashrateRepository, GetDataService getDataService) {
        super(networkHashrateRepository, poolHashrateRepository, getDataService);
    }

    @Scheduled(fixedRate = 10000)
    public void runScheduledTask(){

        System.out.println("Scheduler running");
        getDataService.storeData();
    }
}
