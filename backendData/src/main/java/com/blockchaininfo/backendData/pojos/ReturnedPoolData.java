package com.blockchaininfo.backendData.pojos;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReturnedPoolData {

    private String jsonString;
    private OffsetDateTime dateTime;
    private String poolName;
    private String poolType;
}
