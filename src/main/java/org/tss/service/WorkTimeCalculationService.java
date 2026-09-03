package org.tss.service;

import org.springframework.stereotype.Service;
import org.tss.model.Contract;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class WorkTimeCalculationService {
    private final HolidayService holidayService;

    public WorkTimeCalculationService(HolidayService holidayService) { this.holidayService = holidayService; }

    public double hoursDue(Contract contract, LocalDate start, LocalDate end) {
        long days = start.datesUntil(end.plusDays(1))
                .filter(d -> holidayService.isWorkingDay(d, contract.getWorkingDaysPerWeek(), contract.getFederalState())).count();
        return days * contract.getWorkingHoursPerWeek() / (double) contract.getWorkingDaysPerWeek();
    }

    public double vacationHours(Contract contract) {
        long months = ChronoUnit.MONTHS.between(contract.getStartDate().withDayOfMonth(1),
                contract.getEndDate().withDayOfMonth(1)) + 1;
        return contract.getVacationDaysPerYear() * months / 12.0
                * contract.getWorkingHoursPerWeek() / contract.getWorkingDaysPerWeek();
    }
}
