package org.tss.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tss.model.TimeEntry;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
}