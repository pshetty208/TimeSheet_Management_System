package org.tss.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tss.model.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long> {
}