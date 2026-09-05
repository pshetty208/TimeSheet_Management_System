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
import org.springframework.security.core.Authentication;
import org.tss.service.AccessService;

@RestController
@RequestMapping("/api/timesheets")
public class TimeSheetController {

    private final TimeSheetService timeSheetService;
    private final AccessService access;

    public TimeSheetController(TimeSheetService timeSheetService, AccessService access) {
        this.timeSheetService = timeSheetService;
        this.access = access;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public List<TimeSheet> list(Authentication auth) {
        return timeSheetService.findAll().stream().filter(t -> access.timesheet(t, auth)).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public ResponseEntity<TimeSheet> getById(@PathVariable Long id, Authentication auth) {
        TimeSheet sheet = timeSheetService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found with id: " + id));
        if (!access.timesheet(sheet, auth)) {
            throw new org.tss.exception.UnauthorizedException("Not affiliated with this timesheet");
        }
        return ResponseEntity.ok(sheet);
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

    @PostMapping("/{id}/revoke-employee-signature")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public TimeSheet revoke(@PathVariable Long id) { return timeSheetService.revokeEmployeeSignature(id); }

    @PostMapping("/{id}/request-changes")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ASSISTANT', 'ADMINISTRATOR')")
    public TimeSheet requestChanges(@PathVariable Long id) { return timeSheetService.requestChanges(id); }

    @GetMapping("/{id}/verify-signatures")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public java.util.Map<String, Boolean> verifySignatures(@PathVariable Long id, Authentication auth) {
        TimeSheet sheet = timeSheetService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found with id: " + id));
        if (!access.timesheet(sheet, auth))
            throw new org.tss.exception.UnauthorizedException("Not affiliated with this timesheet");
        return timeSheetService.verifySignatures(id);
    }
}
