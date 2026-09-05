package org.tss.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tss.dto.TimeEntryRequest;
import org.tss.exception.ResourceNotFoundException;
import org.tss.model.TimeEntry;
import org.tss.service.TimeEntryService;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.tss.service.AccessService;
import org.tss.repository.TimeSheetRepository;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;
    private final TimeSheetRepository timeSheets;
    private final AccessService access;

    public TimeEntryController(TimeEntryService timeEntryService, TimeSheetRepository timeSheets, AccessService access) {
        this.timeEntryService = timeEntryService;
        this.timeSheets = timeSheets;
        this.access = access;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public List<TimeEntry> list(Authentication auth) {
        return timeEntryService.findAll().stream().filter(entry -> timeSheets.findByEntriesId(entry.getId())
                .map(sheet -> access.timesheet(sheet, auth)).orElse(false)).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public ResponseEntity<TimeEntry> getById(@PathVariable Long id, Authentication auth) {
        TimeEntry entry = timeEntryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry not found with id: " + id));
        var sheet = timeSheets.findByEntriesId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owning timesheet not found"));
        if (!access.timesheet(sheet, auth))
            throw new org.tss.exception.UnauthorizedException("Not affiliated with this time entry");
        return ResponseEntity.ok(entry);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<TimeEntry> create(@Valid @RequestBody TimeEntryRequest req) {
        TimeEntry te = new TimeEntry();
        te.setDate(req.getDate());
        te.setStartTime(req.getStartTime());
        te.setEndTime(req.getEndTime());
        te.setDescription(req.getDescription());
        te.setReportType(req.getReportType());
        if (req.getTimesheetId() == null) throw new org.tss.exception.ValidationException("timesheetId is required");
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryService.addToTimesheet(req.getTimesheetId(), te));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<TimeEntry> update(@PathVariable Long id, @Valid @RequestBody TimeEntryRequest req) {
        if (req.getTimesheetId() == null) throw new org.tss.exception.ValidationException("timesheetId is required");
        TimeEntry existing = new TimeEntry();
        existing.setDate(req.getDate());
        existing.setStartTime(req.getStartTime());
        existing.setEndTime(req.getEndTime());
        existing.setDescription(req.getDescription());
        existing.setReportType(req.getReportType());
        return ResponseEntity.ok(timeEntryService.updateInTimesheet(req.getTimesheetId(), id, existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
