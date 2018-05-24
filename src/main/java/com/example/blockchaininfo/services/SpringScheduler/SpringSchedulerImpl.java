package com.example.blockchaininfo.services.SpringScheduler;

import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.example.blockchaininfo.services.GetDataService;
import com.example.blockchaininfo.services.SchedulerService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Profile("springScheduler")
@Service
public class SpringSchedulerImpl extends SchedulerService {

    public SpringSchedulerImpl(NetworkHashrateRepository networkHashrateRepository, PoolHashrateRepository poolHashrateRepository, GetDataService getDataService) {
        super(networkHashrateRepository, poolHashrateRepository, getDataService);
    }

    @Scheduled(fixedRate = 10000)
    public void runScheduledTask(){

        getDataService.storeData();
    }
}
