package com.example.blockchaininfo.services;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.OffsetDateTime;
import java.util.List;

public interface FindAndDisplayDataService {

    public List<NetworkHashrate> getAllNetworks();
    public List<PoolHashrate> getAllPools(Long id);
    public String returnNetworkAsJson() throws JsonProcessingException;
}
