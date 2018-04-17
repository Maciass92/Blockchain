package com.example.blockchaininfo.repositories;

import com.example.blockchaininfo.model.NetworkHashrate;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NetworkHashrateRepository extends CrudRepository<NetworkHashrate, Long> {

}
