package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.model.Contract;
import org.tss.model.TimeSheet;
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
        return contractRepository.save(c);
    }

    public Optional<Contract> findById(Long id) {
        return contractRepository.findById(id);
    }

    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    public void delete(Long id) {
        contractRepository.deleteById(id);
    }

}