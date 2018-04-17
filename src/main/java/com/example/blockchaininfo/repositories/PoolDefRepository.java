package com.example.blockchaininfo.repositories;

import com.example.blockchaininfo.model.PoolDef;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PoolDefRepository extends CrudRepository<PoolDef, Long> {

    Optional<PoolDef> findByName(String name);
}
