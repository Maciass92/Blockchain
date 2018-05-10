package com.example.blockchaininfo.pojos;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReturnedStringAndDate {

    private String jsonString;
    private OffsetDateTime dateTime;
    private String poolName;
    private String poolType;
}
