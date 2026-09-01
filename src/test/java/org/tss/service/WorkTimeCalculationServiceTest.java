package org.tss.service;

import org.junit.jupiter.api.Test;
import org.tss.model.Contract;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkTimeCalculationServiceTest {
    private final WorkTimeCalculationService service = new WorkTimeCalculationService(new HolidayService());
    @Test void vacationUsesMonthFormula() {
        Contract c = contract();
        assertEquals(80.0, service.vacationHours(c), 0.001);
    }
    @Test void dueHoursExcludeWeekendsAndNewYearsDay() {
        Contract c = contract();
        assertEquals(16.0, service.hoursDue(c, LocalDate.of(2025,1,1), LocalDate.of(2025,1,7)), 0.001);
    }
    private Contract contract() {
        Contract c = new Contract(); c.setStartDate(LocalDate.of(2025,1,1)); c.setEndDate(LocalDate.of(2025,12,31));
        c.setWorkingHoursPerWeek(20); c.setWorkingDaysPerWeek(5); c.setVacationDaysPerYear(20); return c;
    }
}
