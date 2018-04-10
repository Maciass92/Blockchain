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

    @OneToOne
    @NotNull
    @JoinColumn(name = "network_id")
    private NetworkHashrate networkHashrate;

    @OneToOne
    @NotNull
    @JoinColumn(name = "pool_id")
    private PoolHashrate poolHashrate;

    private double hashrate;
}
