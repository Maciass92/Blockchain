package com.example.blockchaininfo.services;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FindAndDisplayDataService {

    private final NetworkHashrateRepository networkHashrateRepository;
    private final PoolHashrateRepository poolHashrateRepository;

    public List<NetworkHashrate> getAllNetworks(){

        List<NetworkHashrate> networkHashrateList = new ArrayList<>();

        networkHashrateRepository.findAll().iterator().forEachRemaining(networkHashrateList::add);

        return networkHashrateList;
    }

    public List<PoolHashrate> getAllPools(Long id){

        List<PoolHashrate> poolsList = new ArrayList<>();

        poolHashrateRepository.findAllByNetworkId(id).iterator().forEachRemaining(poolsList::add);

        return poolsList;
    }



}