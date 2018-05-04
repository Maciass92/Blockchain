package com.example.blockchaininfo.pojos;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PoolStatus {

    private String name;
    private int errorCounter;
    private Status status;
    private String jsonResponse;
    private OffsetDateTime dateTime;

}
