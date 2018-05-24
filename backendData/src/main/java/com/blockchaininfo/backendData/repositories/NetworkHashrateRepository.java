package com.blockchaininfo.backendData.repositories;

import com.blockchaininfo.backendData.model.NetworkHashrate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface NetworkHashrateRepository extends CrudRepository<NetworkHashrate, Long> {

    @Query("SELECT u FROM NetworkHashrate u WHERE rep_date = :date")
    Optional<NetworkHashrate> findByDate(@Param("date")OffsetDateTime date);

    List<NetworkHashrate> findAll();

}
