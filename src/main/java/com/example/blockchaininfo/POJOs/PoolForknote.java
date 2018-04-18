package com.example.blockchaininfo.POJOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PoolForknote {

    private double hashrate;
}
