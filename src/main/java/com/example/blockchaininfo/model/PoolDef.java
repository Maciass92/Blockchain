package com.example.blockchaininfo.model;

import lombok.Data;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@Entity
public class PoolDef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "date_from", columnDefinition = "timestamp with time zone not null")
    private OffsetDateTime dateFrom;

}
