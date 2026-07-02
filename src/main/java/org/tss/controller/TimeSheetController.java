package org.tss.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tss.model.TimeSheet;
import org.tss.service.TimeSheetService;

import java.util.List;

@RestController
@RequestMapping("/tms/api/timesheets")
public class TimeSheetController {

    private final TimeSheetService timeSheetService;

    public TimeSheetController(TimeSheetService timeSheetService) {
        this.timeSheetService = timeSheetService;
    }

    @GetMapping
    public List<TimeSheet> list() { return timeSheetService.findAll(); }

    @PostMapping
    public TimeSheet create(@RequestBody TimeSheet t) { t.setStatus("IN_PROGRESS"); return timeSheetService.save(t); }

    @PutMapping("/{id}/sign")
    public ResponseEntity<?> sign(@PathVariable Long id) {
        return timeSheetService.findById(id).map(ts -> {
            if ("IN_PROGRESS".equals(ts.getStatus())) {
                ts.setStatus("SIGNED_BY_EMPLOYEE");
                timeSheetService.save(ts);
                return ResponseEntity.ok(ts);
            }
            return ResponseEntity.badRequest().body("Invalid state");
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}