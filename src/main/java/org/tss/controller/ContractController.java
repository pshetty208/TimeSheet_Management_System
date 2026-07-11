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

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SUPERVISOR', 'ASSISTANT', 'SECRETARY', 'ADMINISTRATOR')")
    public List<Contract> list() {
        return contractService.findAll();
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
}