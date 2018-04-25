package com.example.blockchaininfo.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PoolList {

    @JsonProperty("pools")
    private List<PoolDefinition> poolList;
}
