package org.tss.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tss.model.TimeSheet;

public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {
}