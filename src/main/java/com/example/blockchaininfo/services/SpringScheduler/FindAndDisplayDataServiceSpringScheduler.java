package com.example.blockchaininfo.services.SpringScheduler;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.example.blockchaininfo.services.FindAndDisplayDataService;
import com.example.blockchaininfo.services.GetDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;


@Profile("springScheduler")
@Service
public class FindAndDisplayDataServiceSpringScheduler extends FindAndDisplayDataService {

    public FindAndDisplayDataServiceSpringScheduler(NetworkHashrateRepository networkHashrateRepository, PoolHashrateRepository poolHashrateRepository, GetDataService getDataService) {
        super(networkHashrateRepository, poolHashrateRepository, getDataService);
    }

    @Scheduled(fixedRate = 5000)
    public void runScheduledTask(){

        getDataService.storeData();
    }
}
