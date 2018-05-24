package com.example.blockchaininfo.services;

import com.example.blockchaininfo.repositories.NetworkHashrateRepository;
import com.example.blockchaininfo.repositories.PoolHashrateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
public abstract class SchedulerService {

    public final NetworkHashrateRepository networkHashrateRepository;
    public final PoolHashrateRepository poolHashrateRepository;
    public final GetDataService getDataService;

    public abstract void runScheduledTask();
}
