package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.exception.InvalidStateTransitionException;
import org.tss.exception.ResourceNotFoundException;
import org.tss.exception.ValidationException;
import org.tss.model.Contract;
import org.tss.repository.ContractRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ContractService {

    private final ContractRepository contractRepository;

    public ContractService(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public Contract save(Contract c) {
        validateContract(c);
        return contractRepository.save(c);
    }

    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }

    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    public void delete(Long id) {
        Contract c = findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!"PREPARED".equals(c.getStatus())) {
            throw new InvalidStateTransitionException("Can only delete contracts in PREPARED status");
        }
        contractRepository.deleteById(id);
    }

    public Contract startContract(Long id) {
        Contract c = findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!"PREPARED".equals(c.getStatus())) {
            throw new InvalidStateTransitionException("Can only start PREPARED contracts");
        }
        c.setStatus("STARTED");
        return contractRepository.save(c);
    }

    public Contract terminateContract(Long id) {
        Contract c = findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if ("ARCHIVED".equals(c.getStatus())) {
            throw new InvalidStateTransitionException("Cannot terminate archived contracts");
        }
        c.setStatus("TERMINATED");
        return contractRepository.save(c);
    }

    private void validateContract(Contract c) {
        if (c.getEmployee() == null) {
            throw new ValidationException("Employee is required");
        }
        if (c.getWorkingHoursPerWeek() <= 0) {
            throw new ValidationException("Working hours per week must be positive");
        }
        if (c.getStartDate() == null) {
            throw new ValidationException("Start date is required");
        }
        if (c.getEndDate() != null && c.getEndDate().isBefore(c.getStartDate())) {
            throw new ValidationException("End date must be after start date");
        }
        if (c.getVacationEntitlement() < 0) {
            throw new ValidationException("Vacation entitlement cannot be negative");
        }
    }
}