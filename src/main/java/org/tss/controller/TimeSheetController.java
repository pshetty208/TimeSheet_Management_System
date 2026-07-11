package org.tss.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tss.exception.ResourceNotFoundException;
import org.tss.model.TimeSheet;
import org.tss.service.TimeSheetService;

import java.util.List;

@RestController
@RequestMapping("/api/timesheets")
public class TimeSheetController {

    private final TimeSheetService timeSheetService;

    public TimeSheetController(TimeSheetService timeSheetService) {
        this.timeSheetService = timeSheetService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public List<TimeSheet> list() {
        return timeSheetService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public ResponseEntity<TimeSheet> getById(@PathVariable Long id) {
        return timeSheetService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found with id: " + id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT')")
    public ResponseEntity<TimeSheet> create(@Valid @RequestBody TimeSheet t) {
        t.setStatus("IN_PROGRESS");
        return ResponseEntity.status(HttpStatus.CREATED).body(timeSheetService.save(t));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT')")
    public ResponseEntity<TimeSheet> update(@PathVariable Long id, @Valid @RequestBody TimeSheet t) {
        TimeSheet existing = timeSheetService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found with id: " + id));
        if (!"IN_PROGRESS".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Can only update timesheets in IN_PROGRESS status");
        }
        t.setId(existing.getId());
        t.setStatus("IN_PROGRESS");
        return ResponseEntity.ok(timeSheetService.save(t));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeSheetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sign-employee")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<TimeSheet> signByEmployee(@PathVariable Long id) {
        TimeSheet ts = timeSheetService.signByEmployee(id);
        return ResponseEntity.ok(ts);
    }

    @PostMapping("/{id}/sign-supervisor")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMINISTRATOR')")
    public ResponseEntity<TimeSheet> signBySupervisor(@PathVariable Long id) {
        TimeSheet ts = timeSheetService.signBySupervisor(id);
        return ResponseEntity.ok(ts);
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('SECRETARY', 'ADMINISTRATOR')")
    public ResponseEntity<TimeSheet> archive(@PathVariable Long id) {
        TimeSheet ts = timeSheetService.archive(id);
        return ResponseEntity.ok(ts);
    }
}