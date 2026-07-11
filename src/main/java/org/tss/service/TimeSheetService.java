package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.exception.InvalidStateTransitionException;
import org.tss.exception.ResourceNotFoundException;
import org.tss.exception.ValidationException;
import org.tss.model.TimeSheet;
import org.tss.repository.TimeSheetRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TimeSheetService {
    private final TimeSheetRepository timeSheetRepository;

    public TimeSheetService(TimeSheetRepository timeSheetRepository) {
        this.timeSheetRepository = timeSheetRepository;
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
        t.setStatus("SIGNED_BY_EMPLOYEE");
        return timeSheetRepository.save(t);
    }

    public TimeSheet signBySupervisor(Long id) {
        TimeSheet t = findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found"));
        if (!"SIGNED_BY_EMPLOYEE".equals(t.getStatus())) {
            throw new InvalidStateTransitionException("TimeSheet must be signed by employee first");
        }
        t.setStatus("SIGNED_BY_SUPERVISOR");
        return timeSheetRepository.save(t);
    }

    public TimeSheet archive(Long id) {
        TimeSheet t = findById(id).orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found"));
        if (!"SIGNED_BY_SUPERVISOR".equals(t.getStatus())) {
            throw new InvalidStateTransitionException("TimeSheet must be signed by supervisor before archiving");
        }
        t.setStatus("ARCHIVED");
        return timeSheetRepository.save(t);
    }

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
