package com.massage.booking.repository;

import com.massage.booking.entity.MassageService;
import com.massage.booking.entity.enums.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MassageServiceRepository extends JpaRepository<MassageService, Long> {

    List<MassageService> findByActiveTrue();

    List<MassageService> findByCategoryAndActiveTrue(ServiceCategory category);

    boolean existsByNameIgnoreCase(String name);
}
