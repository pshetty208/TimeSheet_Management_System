package org.tss.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class HolidayServiceTest {
    private final HolidayService service = new HolidayService();
    @Test void includesRhinelandPalatinateHolidaysThrough2030() {
        assertTrue(service.holidays(2025, "RP").contains(LocalDate.of(2025, 6, 19)));
        assertTrue(service.holidays(2030, "RP").contains(LocalDate.of(2030, 12, 25)));
    }
    @Test void supportsConfigurableFederalStates() {
        assertTrue(service.holidays(2026, "BY").contains(LocalDate.of(2026, 1, 6)));
        assertFalse(service.holidays(2026, "RP").contains(LocalDate.of(2026, 1, 6)));
    }
    @Test void coversAllGermanFederalStatesAndRegionalRules() {
        for (String state : HolidayService.SUPPORTED_STATES) assertFalse(service.holidays(2030, state).isEmpty());
        assertTrue(service.holidays(2026, "BE").contains(LocalDate.of(2026, 3, 8)));
        assertTrue(service.holidays(2026, "SN").contains(LocalDate.of(2026, 11, 18)));
        assertTrue(service.holidays(2026, "SL").contains(LocalDate.of(2026, 8, 15)));
    }
}
