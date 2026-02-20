package com.massage.booking.repository;

import com.massage.booking.entity.WorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkingDayRepository extends JpaRepository<WorkingDay, Long> {

    Optional<WorkingDay> findByDayOfWeek(Integer dayOfWeek);

    boolean existsByDayOfWeekAndIsActiveTrue(Integer dayOfWeek);
}