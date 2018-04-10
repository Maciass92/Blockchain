package com.example.blockchaininfo.model;


import lombok.Data;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Data
@Entity
public class NetworkHashrate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "rep_date", columnDefinition = "timestamp with time zone not null")
    private OffsetDateTime rep_date;

    private double hashrate;

}
