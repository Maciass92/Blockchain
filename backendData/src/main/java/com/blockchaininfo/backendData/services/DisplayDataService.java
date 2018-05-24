package com.blockchaininfo.backendData.services;

import com.blockchaininfo.backendData.model.PoolHashrate;
import com.blockchaininfo.backendData.repositories.NetworkHashrateRepository;
import com.blockchaininfo.backendData.repositories.PoolHashrateRepository;
import com.blockchaininfo.backendData.model.NetworkHashrate;
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
