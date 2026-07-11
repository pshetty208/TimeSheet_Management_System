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

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final TimeEntryService timeEntryService;

    public TimeEntryController(TimeEntryService timeEntryService) {
        this.timeEntryService = timeEntryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public List<TimeEntry> list() {
        return timeEntryService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public ResponseEntity<TimeEntry> getById(@PathVariable Long id) {
        return timeEntryService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry not found with id: " + id));
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
        return ResponseEntity.status(HttpStatus.CREATED).body(timeEntryService.save(te));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<TimeEntry> update(@PathVariable Long id, @Valid @RequestBody TimeEntryRequest req) {
        TimeEntry existing = timeEntryService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry not found with id: " + id));
        existing.setDate(req.getDate());
        existing.setStartTime(req.getStartTime());
        existing.setEndTime(req.getEndTime());
        existing.setDescription(req.getDescription());
        existing.setReportType(req.getReportType());
        return ResponseEntity.ok(timeEntryService.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
