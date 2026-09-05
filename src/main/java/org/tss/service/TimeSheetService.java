package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.exception.InvalidStateTransitionException;
import org.tss.exception.ResourceNotFoundException;
import org.tss.exception.ValidationException;
import org.tss.model.TimeSheet;
import org.tss.repository.TimeSheetRepository;
import org.tss.repository.ContractRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;

@Service
public class TimeSheetService {
    private final TimeSheetRepository timeSheetRepository;
    private final ContractRepository contractRepository;
    private final DigitalSignatureService signatures;

    public TimeSheetService(TimeSheetRepository timeSheetRepository, ContractRepository contractRepository) {
        this(timeSheetRepository, contractRepository, new DigitalSignatureService());
    }

    @Autowired
    public TimeSheetService(TimeSheetRepository timeSheetRepository, ContractRepository contractRepository,
                            DigitalSignatureService signatures) {
        this.timeSheetRepository = timeSheetRepository;
        this.contractRepository = contractRepository;
        this.signatures = signatures;
    }

    public TimeSheet save(TimeSheet t) {
        validateTimeSheet(t);
        return timeSheetRepository.save(t);
    }

    public Optional<TimeSheet> findById(Long id) {
        return timeSheetRepository.findById(id);
    }

    public List<TimeSheet> findAll() {
        return timeSheetRepository.findAll();
    }

    public void delete(Long id) {
        TimeSheet t = findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found"));
        if (!"IN_PROGRESS".equals(t.getStatus())) {
            throw new InvalidStateTransitionException("Can only delete timesheets in IN_PROGRESS status");
        }
        timeSheetRepository.deleteById(id);
    }

    public TimeSheet signByEmployee(Long id) {
        TimeSheet t = findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found"));
        if (!"IN_PROGRESS".equals(t.getStatus())) {
            throw new InvalidStateTransitionException("Can only sign timesheets in IN_PROGRESS status");
        }
        if (!isAdmin() && !current().equals(t.getContract().getEmployee().getUsername()))
            throw new org.tss.exception.UnauthorizedException("Only the contract employee may sign");
        t.setStatus("SIGNED_BY_EMPLOYEE");
        t.setSignedByEmployee(LocalDate.now());
        var signed = signatures.sign(signatureValue(t, current(), t.getSignedByEmployee()));
        t.setEmployeeSignature(signed.signature());
        t.setEmployeeSignaturePublicKey(signed.publicKey());
        return timeSheetRepository.save(t);
    }

    public TimeSheet signBySupervisor(Long id) {
        TimeSheet t = findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found"));
        if (!"SIGNED_BY_EMPLOYEE".equals(t.getStatus())) {
            throw new InvalidStateTransitionException("TimeSheet must be signed by employee first");
        }
        if (!isAdmin() && !current().equals(t.getContract().getSupervisor().getUsername()))
            throw new org.tss.exception.UnauthorizedException("Only the contract supervisor may sign");
        t.setStatus("SIGNED_BY_SUPERVISOR");
        t.setSignedBySupervisor(LocalDate.now());
        var signed = signatures.sign(signatureValue(t, current(), t.getSignedBySupervisor()));
        t.setSupervisorSignature(signed.signature());
        t.setSupervisorSignaturePublicKey(signed.publicKey());
        return timeSheetRepository.save(t);
    }

    public TimeSheet archive(Long id) {
        TimeSheet t = findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found"));
        if (!"SIGNED_BY_SUPERVISOR".equals(t.getStatus())) {
            throw new InvalidStateTransitionException("TimeSheet must be signed by supervisor before archiving");
        }
        if (!isAdmin() && t.getContract().getSecretaries().stream().noneMatch(s -> current().equals(s.getUsername())))
            throw new org.tss.exception.UnauthorizedException("Only an assigned secretary may archive");
        t.setStatus("ARCHIVED");
        TimeSheet saved = timeSheetRepository.save(t);
        var contractSheets = timeSheetRepository.findByContractIdOrderByPeriodStart(t.getContract().getId());
        if (!contractSheets.isEmpty() && contractSheets.stream().allMatch(s -> "ARCHIVED".equals(s.getStatus()))) {
            t.getContract().setStatus("ARCHIVED");
            contractRepository.save(t.getContract());
        }
        return saved;
    }

    public TimeSheet revokeEmployeeSignature(Long id) {
        TimeSheet t = requireStatus(id, "SIGNED_BY_EMPLOYEE");
        if (!isAdmin() && !current().equals(t.getContract().getEmployee().getUsername()))
            throw new org.tss.exception.UnauthorizedException("Only the contract employee may revoke");
        t.setStatus("IN_PROGRESS"); t.setSignedByEmployee(null); t.setEmployeeSignature(null); t.setEmployeeSignaturePublicKey(null);
        return timeSheetRepository.save(t);
    }

    public TimeSheet requestChanges(Long id) {
        TimeSheet t = requireStatus(id, "SIGNED_BY_EMPLOYEE");
        boolean allowed = current().equals(t.getContract().getSupervisor().getUsername())
                || t.getContract().getAssistants().stream().anyMatch(a -> current().equals(a.getUsername()));
        if (!isAdmin() && !allowed) throw new org.tss.exception.UnauthorizedException("Not assigned to this contract");
        t.setStatus("IN_PROGRESS"); t.setSignedByEmployee(null); t.setEmployeeSignature(null); t.setEmployeeSignaturePublicKey(null);
        return timeSheetRepository.save(t);
    }

    public Map<String, Boolean> verifySignatures(Long id) {
        TimeSheet t = findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found"));
        String employee = t.getContract().getEmployee().getUsername();
        String supervisor = t.getContract().getSupervisor().getUsername();
        return Map.of(
                "employee", signatures.verify(signatureValue(t, employee, t.getSignedByEmployee()),
                        t.getEmployeeSignature(), t.getEmployeeSignaturePublicKey()),
                "supervisor", signatures.verify(signatureValue(t, supervisor, t.getSignedBySupervisor()),
                        t.getSupervisorSignature(), t.getSupervisorSignaturePublicKey()));
    }

    private TimeSheet requireStatus(Long id, String status) {
        TimeSheet t = findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found"));
        if (!status.equals(t.getStatus())) throw new InvalidStateTransitionException("Timesheet must be " + status);
        return t;
    }

    private String signatureValue(TimeSheet t, String signer, LocalDate date) {
        StringBuilder value = new StringBuilder();
        value.append(t.getId()).append('|').append(t.getContract().getId()).append('|')
                .append(t.getPeriodStart()).append('|').append(t.getPeriodEnd()).append('|')
                .append(t.getHoursDue()).append('|').append(signer).append('|').append(date);
        t.getEntries().forEach(e -> value.append('|').append(e.getId()).append(':').append(e.getDate())
                .append(':').append(e.getStartTime()).append(':').append(e.getEndTime())
                .append(':').append(e.getReportType()).append(':').append(e.getHours())
                .append(':').append(e.getDescription()));
        return value.toString();
    }
    private String current() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
    private boolean isAdmin() { return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMINISTRATOR".equals(a.getAuthority())); }

    private void validateTimeSheet(TimeSheet t) {
        if (t.getContract() == null) {
            throw new ValidationException("Contract is required");
        }
        if (t.getPeriodStart() == null) {
            throw new ValidationException("Period start is required");
        }
        if (t.getPeriodEnd() == null) {
            throw new ValidationException("Period end is required");
        }
        if (t.getPeriodEnd().isBefore(t.getPeriodStart())) {
            throw new ValidationException("Period end must be after period start");
        }
    }
}
