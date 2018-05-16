package com.example.blockchaininfo.services;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface FindAndDisplayDataService {

    List<NetworkHashrate> getAllNetworks();
    List<PoolHashrate> getAllPools(Long id);
    String returnNetworkAsJson() throws JsonProcessingException;
}
