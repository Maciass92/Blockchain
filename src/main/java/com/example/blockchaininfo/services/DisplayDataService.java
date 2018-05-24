package com.example.blockchaininfo.services;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DisplayDataService {

    public final NetworkHashrateRepository networkHashrateRepository;
    public final PoolHashrateRepository poolHashrateRepository;
    public final GetDataService getDataService;

    public List<NetworkHashrate> getAllNetworks(){

        return networkHashrateRepository.findAll();
    }

    public List<PoolHashrate> getAllPools(Long id){

        return poolHashrateRepository.findAllByNetworkId(id);
    }

    public String returnNetworkAsJson() throws JsonProcessingException{

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(this.getAllNetworks());
    }

    public String returnPoolsAsJson(Long id) throws JsonProcessingException{

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(this.getAllPools(id));
    }
}
