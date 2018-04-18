package com.example.blockchaininfo.POJOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PoolJson {

    @JsonProperty("pool")
    private PoolForknote poolForknote;
    @JsonProperty("pool_statistics")
    private PoolNodeJs pool_nodeJs;
}
