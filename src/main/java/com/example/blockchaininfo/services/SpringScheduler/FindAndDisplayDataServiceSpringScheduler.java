package com.example.blockchaininfo.services.SpringScheduler;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.example.blockchaininfo.services.FindAndDisplayDataService;
import com.example.blockchaininfo.services.GetDataService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Profile("springScheduler")
@Service
public class FindAndDisplayDataServiceSpringScheduler implements FindAndDisplayDataService {

    private final NetworkHashrateRepository networkHashrateRepository;
    private final PoolHashrateRepository poolHashrateRepository;
    private final GetDataService getDataService;

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

    @Scheduled(fixedRate = 5000)
    public void runScheduledTask(){

        this.getDataService.storeData();
    }
}
