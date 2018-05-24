package com.blockchaininfo.backendData.pojos;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class PoolDefinition {

    private String name;
    private String url;
    private String api;
    private String type;

}
