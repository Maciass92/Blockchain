package com.example.blockchaininfo.services;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FindAndDisplayDataService {

    private final NetworkHashrateRepository networkHashrateRepository;
    private final PoolHashrateRepository poolHashrateRepository;

    public List<NetworkHashrate> getAllNetworks(){

        List<NetworkHashrate> networkHashrateList = networkHashrateRepository.findAll();

        return networkHashrateList;
    }

    public List<PoolHashrate> getAllPools(Long id){

        List<PoolHashrate> poolsList = new ArrayList<>();

        poolHashrateRepository.findAllByNetworkId(id).iterator().forEachRemaining(poolsList::add);

        return poolsList;
    }

    public String returnNetworkAsJson() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(this.getAllNetworks());
    }

    public String formatDate(OffsetDateTime dateTime){

        return DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss:SSS").format(dateTime);
    }

    public double formatHashrate(double hashrate){

        return hashrate/1000.0;
    }

}
