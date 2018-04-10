package com.example.blockchaininfo.model;

import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Data
@Entity
public class PoolDef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String name;

    @Column(name = "date_from", columnDefinition = "timestamp with time zone not null")
    private ZonedDateTime date_from;

}
