package com.example.blockchaininfo.model;


import lombok.Data;

import javax.persistence.*;

import java.time.OffsetDateTime;

@Data
@Entity
public class NetworkHashrate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rep_date", columnDefinition = "timestamp with time zone not null")
    private OffsetDateTime repDate;

    private double hashrate;
}
