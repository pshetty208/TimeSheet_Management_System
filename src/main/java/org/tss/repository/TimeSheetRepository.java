package org.tss.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.tss.model.TimeSheet;

import java.util.List;

public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {
    
    @Query("SELECT ts FROM TimeSheet ts WHERE ts.status = 'IN_PROGRESS'")
    List<TimeSheet> findAllInProgress();

    @Query("SELECT ts FROM TimeSheet ts WHERE ts.status = 'SIGNED_BY_EMPLOYEE'")
    List<TimeSheet> findAllSignedByEmployee();

    @Query("SELECT ts FROM TimeSheet ts WHERE ts.status = 'SIGNED_BY_SUPERVISOR'")
    List<TimeSheet> findAllSignedBySupervisor();
}
