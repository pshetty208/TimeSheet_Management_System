package org.tss.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.tss.exception.ResourceNotFoundException;
import org.tss.service.ContractService;
import org.tss.service.TimeSheetService;
import org.tss.service.AccessService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/print")
public class PrintController {
    private final ContractService contracts;
    private final TimeSheetService sheets;
    private final AccessService access;
    public PrintController(ContractService contracts, TimeSheetService sheets, AccessService access) { this.contracts = contracts; this.sheets = sheets; this.access = access; }

    @GetMapping(value="/contracts/{id}", produces=MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyRole('SECRETARY','ADMINISTRATOR')")
    public String contract(@PathVariable Long id, Authentication auth) {
        var c = contracts.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!access.contractSecretary(c, auth)) throw new org.tss.exception.UnauthorizedException("Not assigned to this contract");
        return page("Contract " + c.getId(), "<h1>" + esc(c.getName()) + "</h1><p>Employee: " + esc(c.getEmployee().getUsername())
                + "</p><p>Supervisor: " + esc(c.getSupervisor().getUsername()) + "</p><p>Period: " + c.getStartDate() + " - " + c.getEndDate()
                + "</p><p>Hours/week: " + c.getWorkingHoursPerWeek() + "</p><p>Status: " + c.getStatus() + "</p>");
    }

    @GetMapping(value="/timesheets/{id}", produces=MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasAnyRole('SECRETARY','ADMINISTRATOR')")
    public String sheet(@PathVariable Long id, Authentication auth) {
        var t = sheets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Timesheet not found"));
        if (!access.contractSecretary(t.getContract(), auth)) {
            throw new org.tss.exception.UnauthorizedException("Not assigned to this contract");
        }
        StringBuilder rows = new StringBuilder();
        t.getEntries().forEach(e -> rows.append("<tr><td>").append(e.getDate()).append("</td><td>").append(esc(e.getReportType()))
                .append("</td><td>").append(esc(e.getDescription())).append("</td><td>").append(e.getHours()).append("</td></tr>"));
        return page("Timesheet " + t.getId(), "<h1>Timesheet</h1><p>Period: " + t.getPeriodStart() + " - " + t.getPeriodEnd()
                + "</p><table><tr><th>Date</th><th>Type</th><th>Description</th><th>Hours</th></tr>" + rows + "</table>");
    }
    private String esc(String text) { return HtmlUtils.htmlEscape(text == null ? "" : text); }
    private String page(String title, String body) { return "<!doctype html><html><head><title>"+esc(title)+"</title><style>body{font:14px sans-serif;margin:2cm}table{border-collapse:collapse;width:100%}td,th{border:1px solid #777;padding:8px}@media print{button{display:none}}</style></head><body><button onclick=\"print()\">Print</button>"+body+"</body></html>"; }
}
