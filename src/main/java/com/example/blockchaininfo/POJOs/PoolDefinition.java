package com.example.blockchaininfo.POJOs;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class PoolDefinition {

    private String name;
    private String url;
    private String api;
    private String type;
    private Long id;

}
