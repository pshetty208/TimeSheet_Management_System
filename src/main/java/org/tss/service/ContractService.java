package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.exception.InvalidStateTransitionException;
import org.tss.exception.ResourceNotFoundException;
import org.tss.exception.ValidationException;
import org.tss.model.Contract;
import org.tss.repository.ContractRepository;
import org.tss.repository.TimeSheetRepository;
import org.tss.dto.ContractStatistics;
import org.tss.dto.ContractTerminationPreview;
import org.tss.model.TimeSheet;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;

import java.util.List;
import java.util.Optional;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final TimeSheetRepository timeSheetRepository;
    private final WorkTimeCalculationService calculations;

    public ContractService(ContractRepository contractRepository, TimeSheetRepository timeSheetRepository,
                           WorkTimeCalculationService calculations) {
        this.contractRepository = contractRepository;
        this.timeSheetRepository = timeSheetRepository;
        this.calculations = calculations;
    }

    public Contract save(Contract c) {
        if (c.getStatus() == null || c.getStatus().isBlank()) {
            c.setStatus("PREPARED");
        }
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

    public Contract updatePrepared(Contract existing, Contract input) {
        if (!"PREPARED".equals(existing.getStatus())) throw new InvalidStateTransitionException("Can only update PREPARED contracts");
        existing.setName(input.getName());
        existing.setEmployee(input.getEmployee()); existing.setSupervisor(input.getSupervisor());
        existing.setAssistants(input.getAssistants()); existing.setSecretaries(input.getSecretaries());
        existing.setStartDate(input.getStartDate()); existing.setEndDate(input.getEndDate());
        existing.setFrequency(input.getFrequency()); existing.setWorkingHoursPerWeek(input.getWorkingHoursPerWeek());
        existing.setWorkingDaysPerWeek(input.getWorkingDaysPerWeek()); existing.setVacationDaysPerYear(input.getVacationDaysPerYear());
        existing.setArchiveDurationMonths(input.getArchiveDurationMonths()); existing.setFederalState(input.getFederalState());
        return save(existing);
    }

    @Transactional
    public Contract startContract(Long id) {
        Contract c = findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!"PREPARED".equals(c.getStatus())) {
            throw new InvalidStateTransitionException("Can only start PREPARED contracts");
        }
        c.setStatus("STARTED");
        Contract saved = contractRepository.save(c);
        generateTimeSheets(saved);
        return saved;
    }

    @Transactional
    public Contract terminateContract(Long id) {
        Contract c = findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!"STARTED".equals(c.getStatus())) {
            throw new InvalidStateTransitionException("Can only terminate STARTED contracts");
        }
        List<TimeSheet> sheets = timeSheetRepository.findByContractIdOrderByPeriodStart(id);
        ContractTerminationPreview preview = terminationPreview(id);
        if (!preview.allowed()) throw new InvalidStateTransitionException(preview.message());
        sheets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).forEach(timeSheetRepository::delete);
        c.setStatus("TERMINATED");
        c.setTerminationDate(LocalDate.now());
        return contractRepository.save(c);
    }

    public ContractStatistics statistics(Long id) {
        Contract c = findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        List<TimeSheet> sheets = timeSheetRepository.findByContractIdOrderByPeriodStart(id);
        double due = sheets.stream().mapToDouble(TimeSheet::getHoursDue).sum();
        double reported = sheets.stream().flatMap(t -> t.getEntries().stream()).mapToDouble(e -> e.getHours()).sum();
        double vacationReported = sheets.stream().flatMap(t -> t.getEntries().stream())
                .filter(e -> "VACATION".equals(e.getReportType())).mapToDouble(e -> e.getHours()).sum();
        return new ContractStatistics(id, due, reported, due - reported,
                calculations.vacationHours(c), vacationReported);
    }

    public ContractTerminationPreview terminationPreview(Long id) {
        Contract c = findById(id).orElseThrow(() -> new ResourceNotFoundException("Contract not found"));
        if (!"STARTED".equals(c.getStatus())) return new ContractTerminationPreview(id, false, 0, 0, "Only STARTED contracts can be terminated");
        List<TimeSheet> sheets = timeSheetRepository.findByContractIdOrderByPeriodStart(id);
        boolean allowed = sheets.stream().allMatch(t -> List.of("SIGNED_BY_SUPERVISOR", "IN_PROGRESS", "ARCHIVED").contains(t.getStatus()));
        int open = (int) sheets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        int entered = (int) sheets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus()) && !t.getEntries().isEmpty()).count();
        String message = !allowed ? "Employee-signed timesheets must be approved or returned first"
                : entered > 0 ? entered + " in-progress timesheet(s) contain entries and will be deleted"
                : open + " in-progress timesheet(s) will be deleted";
        return new ContractTerminationPreview(id, allowed, open, entered, message);
    }

    private void generateTimeSheets(Contract c) {
        LocalDate cursor = c.getStartDate();
        while (!cursor.isAfter(c.getEndDate())) {
            LocalDate end;
            if ("WEEKLY".equalsIgnoreCase(c.getFrequency())) {
                end = cursor.plusDays(7 - cursor.getDayOfWeek().getValue());
            } else {
                end = YearMonth.from(cursor).atEndOfMonth();
            }
            if (end.isAfter(c.getEndDate())) end = c.getEndDate();
            TimeSheet sheet = new TimeSheet();
            sheet.setContract(c); sheet.setPeriodStart(cursor); sheet.setPeriodEnd(end);
            sheet.setStatus("IN_PROGRESS"); sheet.setHoursDue(calculations.hoursDue(c, cursor, end));
            timeSheetRepository.save(sheet);
            cursor = end.plusDays(1);
        }
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
        if (c.getEndDate() == null) throw new ValidationException("End date is required");
        if (c.getStartDate().getDayOfMonth() != 1 || !c.getEndDate().equals(YearMonth.from(c.getEndDate()).atEndOfMonth()))
            throw new ValidationException("Contract dates must cover complete months");
        if (!"WEEKLY".equalsIgnoreCase(c.getFrequency()) && !"MONTHLY".equalsIgnoreCase(c.getFrequency()))
            throw new ValidationException("Frequency must be WEEKLY or MONTHLY");
        if (c.getWorkingDaysPerWeek() < 1 || c.getWorkingDaysPerWeek() > 7)
            throw new ValidationException("Working days per week must be between 1 and 7");
        if (c.getVacationDaysPerYear() < 0)
            throw new ValidationException("Vacation days per year cannot be negative");
        if (c.getVacationEntitlement() < 0) {
            throw new ValidationException("Vacation entitlement cannot be negative");
        }
        if (c.getFederalState() == null || !HolidayService.SUPPORTED_STATES.contains(c.getFederalState().toUpperCase()))
            throw new ValidationException("Federal state must be a valid German state code");
        if (c.getArchiveDurationMonths() < 1) throw new ValidationException("Archive duration must be at least one month");
        if (c.getSupervisor() == null || !c.getSupervisor().isUniversityStaff())
            throw new ValidationException("Supervisor must be university staff");
        if (c.getAssistants().stream().anyMatch(a -> !a.isUniversityStaff()) || c.getSecretaries().stream().anyMatch(s -> !s.isUniversityStaff()))
            throw new ValidationException("Assistants and secretaries must be university staff");
        c.setVacationEntitlement(calculations.vacationHours(c));
    }
}
