package org.tss.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tss.exception.ResourceNotFoundException;
import org.tss.model.Contract;
import org.tss.service.ContractService;

import java.util.List;
import org.tss.dto.ContractStatistics;
import org.springframework.security.core.Authentication;
import org.tss.service.AccessService;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;
    private final AccessService access;

    public ContractController(ContractService contractService, AccessService access) {
        this.contractService = contractService;
        this.access = access;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public List<Contract> list(Authentication auth) {
        return contractService.findAll().stream().filter(c -> access.contract(c, auth)).toList();
    }

    @GetMapping("/{id}")
    public Contract get(@PathVariable Long id, Authentication auth) {
        Contract c = contractService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!access.contract(c, auth)) throw new org.tss.exception.UnauthorizedException("Not affiliated with this contract");
        return c;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ASSISTANT', 'ADMINISTRATOR')")
    public ResponseEntity<Contract> create(@Valid @RequestBody Contract c) {
        c.setStatus("PREPARED");
        return ResponseEntity.status(HttpStatus.CREATED).body(contractService.save(c));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ASSISTANT', 'ADMINISTRATOR')")
    public ResponseEntity<Contract> update(@PathVariable Long id, @Valid @RequestBody Contract c) {
        Contract existing = contractService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with id: " + id));
        if (!"PREPARED".equals(existing.getStatus())) {
            throw new IllegalArgumentException("Can only update contracts in PREPARED status");
        }
        c.setId(existing.getId());
        return ResponseEntity.ok(contractService.save(c));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ASSISTANT', 'ADMINISTRATOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ASSISTANT', 'ADMINISTRATOR')")
    public ResponseEntity<Contract> start(@PathVariable Long id) {
        Contract c = contractService.startContract(id);
        return ResponseEntity.ok(c);
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMINISTRATOR')")
    public ResponseEntity<Contract> terminate(@PathVariable Long id) {
        Contract c = contractService.terminateContract(id);
        return ResponseEntity.ok(c);
    }

    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public ContractStatistics statistics(@PathVariable Long id, Authentication auth) {
        Contract c = contractService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!access.contract(c, auth)) throw new org.tss.exception.UnauthorizedException("Not affiliated with this contract");
        return contractService.statistics(id);
    }
}
