package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.exception.ResourceNotFoundException;
import org.tss.exception.ValidationException;
import org.tss.model.TimeEntry;
import org.tss.repository.TimeEntryRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;

    public TimeEntryService(TimeEntryRepository timeEntryRepository) {
        this.timeEntryRepository = timeEntryRepository;
    }

    public TimeEntry save(TimeEntry te) {
        validateTimeEntry(te);
        calculateHours(te);
        return timeEntryRepository.save(te);
    }

    public Optional<TimeEntry> findById(Long id) {
        return timeEntryRepository.findById(id);
    }

    public List<TimeEntry> findAll() {
        return timeEntryRepository.findAll();
    }

    public void delete(Long id) {
        if (!timeEntryRepository.existsById(id)) {
            throw new ResourceNotFoundException("TimeEntry not found with id: " + id);
        }
        timeEntryRepository.deleteById(id);
    }

    private void validateTimeEntry(TimeEntry te) {
        if (te.getDate() == null) {
            throw new ValidationException("Date is required");
        }
        if (te.getReportType() == null || te.getReportType().isEmpty()) {
            throw new ValidationException("Report type is required");
        }
        if (!isValidReportType(te.getReportType())) {
            throw new ValidationException("Invalid report type: " + te.getReportType() + ". Valid types: WORK, VACATION, SICK_LEAVE");
        }

        if ("WORK".equals(te.getReportType())) {
            if (te.getStartTime() == null || te.getEndTime() == null) {
                throw new ValidationException("Start and end times are required for work entries");
            }
            if (te.getEndTime().isBefore(te.getStartTime())) {
                throw new ValidationException("End time must be after start time");
            }
        }
    }

    private void calculateHours(TimeEntry te) {
        if ("WORK".equals(te.getReportType()) && te.getStartTime() != null && te.getEndTime() != null) {
            long minutes = java.time.temporal.ChronoUnit.MINUTES.between(te.getStartTime(), te.getEndTime());
            te.setHours(minutes / 60.0);
        } else if ("VACATION".equals(te.getReportType()) || "SICK_LEAVE".equals(te.getReportType())) {
            te.setHours(8.0);
        }
    }

    private boolean isValidReportType(String type) {
        return type.equals("WORK") || type.equals("VACATION") || type.equals("SICK_LEAVE");
    }
}
