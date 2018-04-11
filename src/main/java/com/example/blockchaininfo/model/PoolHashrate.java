package com.example.blockchaininfo.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class PoolHashrate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "network_Id")
    private NetworkHashrate networkHashrate;

    @OneToOne
    @NotNull
    @JoinColumn(name = "pool_Id")
    private PoolDef poolDef;

    private double hashrate;
}
