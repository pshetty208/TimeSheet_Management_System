package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.exception.ResourceNotFoundException;
import org.tss.exception.ValidationException;
import org.tss.model.TimeEntry;
import org.tss.repository.TimeEntryRepository;
import org.tss.repository.TimeSheetRepository;
import org.tss.model.TimeSheet;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@Service
public class TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final WorkTimeCalculationService calculations;
    private final AccessService access;

    public TimeEntryService(TimeEntryRepository timeEntryRepository, TimeSheetRepository timeSheetRepository,
                            WorkTimeCalculationService calculations, AccessService access) {
        this.timeEntryRepository = timeEntryRepository;
        this.timeSheetRepository = timeSheetRepository;
        this.calculations = calculations;
        this.access = access;
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
        TimeSheet owner = timeSheetRepository.findByEntriesId(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry not found with id: " + id));
        editableSheet(owner.getId());
        owner.getEntries().removeIf(e -> id.equals(e.getId()));
        timeSheetRepository.save(owner);
    }

    public TimeEntry addToTimesheet(Long timesheetId, TimeEntry entry) {
        TimeSheet sheet = editableSheet(timesheetId);
        validateTimeEntry(entry); calculateHours(entry);
        validateEntryPeriod(sheet, entry);
        enforceVacationLimit(sheet, entry, null);
        sheet.getEntries().add(entry);
        timeSheetRepository.save(sheet);
        return entry;
    }

    public TimeEntry updateInTimesheet(Long timesheetId, Long entryId, TimeEntry values) {
        TimeSheet sheet = editableSheet(timesheetId);
        TimeEntry entry = sheet.getEntries().stream().filter(e -> entryId.equals(e.getId())).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Entry not found in timesheet"));
        entry.setDate(values.getDate()); entry.setStartTime(values.getStartTime()); entry.setEndTime(values.getEndTime());
        entry.setDescription(values.getDescription()); entry.setReportType(values.getReportType());
        validateTimeEntry(entry); calculateHours(entry);
        validateEntryPeriod(sheet, entry);
        enforceVacationLimit(sheet, entry, entryId);
        timeSheetRepository.save(sheet); return entry;
    }

    public void deleteFromTimesheet(Long timesheetId, Long entryId) {
        TimeSheet sheet = editableSheet(timesheetId);
        if (!sheet.getEntries().removeIf(e -> entryId.equals(e.getId())))
            throw new ResourceNotFoundException("Entry not found in timesheet");
        timeSheetRepository.save(sheet);
    }

    private TimeSheet editableSheet(Long id) {
        TimeSheet sheet = timeSheetRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Timesheet not found"));
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !access.timesheet(sheet, authentication))
            throw new org.tss.exception.UnauthorizedException("Not affiliated with this timesheet");
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRATOR".equals(a.getAuthority()));
        if (!admin && !authentication.getName().equals(sheet.getContract().getEmployee().getUsername()))
            throw new org.tss.exception.UnauthorizedException("Only the contract employee may manage entries");
        if (!"IN_PROGRESS".equals(sheet.getStatus()) || !"STARTED".equals(sheet.getContract().getStatus()))
            throw new ValidationException("Entries may only change on IN_PROGRESS timesheets of STARTED contracts");
        return sheet;
    }

    private void enforceVacationLimit(TimeSheet sheet, TimeEntry candidate, Long replacedEntryId) {
        if (!"VACATION".equals(candidate.getReportType())) return;
        double used = timeSheetRepository.findByContractIdOrderByPeriodStart(sheet.getContract().getId()).stream()
                .flatMap(s -> s.getEntries().stream())
                .filter(e -> "VACATION".equals(e.getReportType()))
                .filter(e -> replacedEntryId == null || !replacedEntryId.equals(e.getId()))
                .mapToDouble(TimeEntry::getHours).sum();
        if (used + candidate.getHours() > calculations.vacationHours(sheet.getContract()))
            throw new ValidationException("Vacation entitlement exceeded");
    }

    private void validateEntryPeriod(TimeSheet sheet, TimeEntry entry) {
        if (entry.getDate().isBefore(sheet.getPeriodStart()) || entry.getDate().isAfter(sheet.getPeriodEnd()))
            throw new ValidationException("Entry date must be within the timesheet period");
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
