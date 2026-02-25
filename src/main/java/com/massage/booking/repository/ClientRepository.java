package com.massage.booking.repository;

import com.massage.booking.entity.Client;
import com.massage.booking.entity.valueobject.Phone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByPhone(Phone phone);

    Optional<Client> findByPhone(Phone phone);

    // FIX #1: Required to resolve userId → client record
    Optional<Client> findByUserId(Long userId);

    Page<Client> findByActiveTrue(Pageable pageable);

    @Query("SELECT c FROM Client c WHERE c.active = :active AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "c.phone.value LIKE CONCAT('%', :search, '%'))")
    Page<Client> searchClients(@Param("search") String search,
                               @Param("active") Boolean active,
                               Pageable pageable);
}
