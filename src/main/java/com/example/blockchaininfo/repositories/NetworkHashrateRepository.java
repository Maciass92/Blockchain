package com.example.blockchaininfo.repositories;

import com.example.blockchaininfo.model.NetworkHashrate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface NetworkHashrateRepository extends CrudRepository<NetworkHashrate, Long> {

    @Query("SELECT u FROM NetworkHashrate u WHERE rep_date = :date")
    Optional<NetworkHashrate> findByDate(@Param("date")OffsetDateTime date);

    Page<NetworkHashrate> findAll(Pageable pageable);

}
