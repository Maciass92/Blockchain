package com.blockchaininfo.backendData.services;

import com.blockchaininfo.backendData.repositories.NetworkHashrateRepository;
import com.blockchaininfo.backendData.repositories.PoolHashrateRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SchedulerService {

    public final NetworkHashrateRepository networkHashrateRepository;
    public final PoolHashrateRepository poolHashrateRepository;
    public final GetDataService getDataService;

    public abstract void runScheduledTask();
}
