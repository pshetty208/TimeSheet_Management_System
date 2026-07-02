package org.tss.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tss.model.Contract;
import org.tss.service.ContractService;

import java.util.List;

@RestController
@RequestMapping("/tsm/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public List<Contract> list() { return contractService.findAll(); }

    @PostMapping
    public Contract create(@RequestBody Contract c) { c.setStatus("PREPARED"); return contractService.save(c); }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Contract c) {
        return contractService.findById(id).map(existing -> {
            c.setId(existing.getId());
            return ResponseEntity.ok(contractService.save(c));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ResponseEntity.ok().build();
    }
}