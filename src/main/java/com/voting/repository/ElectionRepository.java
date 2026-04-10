package com.voting.repository;

import com.voting.model.Election;
import com.voting.model.ElectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ElectionRepository extends JpaRepository<Election, Long> {

    List<Election> findByStatus(ElectionStatus status);

    List<Election> findByStatusOrderByStartTimeDesc(ElectionStatus status);

    @Query("SELECT e FROM Election e WHERE e.status = 'ACTIVE' AND e.endTime < :now")
    List<Election> findExpiredActiveElections(LocalDateTime now);

    @Query("SELECT e FROM Election e WHERE e.status = 'DRAFT' AND e.startTime <= :now")
    List<Election> findElectionsReadyToStart(LocalDateTime now);
}
