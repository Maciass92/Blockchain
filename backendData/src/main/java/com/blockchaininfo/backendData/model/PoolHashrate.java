package com.blockchaininfo.backendData.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Entity
public class PoolHashrate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
