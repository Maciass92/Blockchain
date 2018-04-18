package com.example.blockchaininfo.repositories;

import com.example.blockchaininfo.model.PoolHashrate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PoolHashrateRepository extends CrudRepository<PoolHashrate, Long> {

    @Query("SELECT u FROM PoolHashrate u WHERE network_id = :id")
    List<PoolHashrate> findAllByNetworkId(@Param("id") Long id);
}
