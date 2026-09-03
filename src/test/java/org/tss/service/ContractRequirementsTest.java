package org.tss.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.tss.exception.InvalidStateTransitionException;
import org.tss.model.Contract;
import org.tss.model.TimeEntry;
import org.tss.model.TimeSheet;
import org.tss.model.User;
import org.tss.repository.ContractRepository;
import org.tss.repository.TimeSheetRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContractRequirementsTest {
    ContractRepository contracts; TimeSheetRepository sheets; WorkTimeCalculationService calculations; ContractService service;

    @BeforeEach void setup() {
        contracts=mock(ContractRepository.class); sheets=mock(TimeSheetRepository.class); calculations=mock(WorkTimeCalculationService.class);
        service=new ContractService(contracts,sheets,calculations);
        when(contracts.save(any())).thenAnswer(i->i.getArgument(0)); when(sheets.save(any())).thenAnswer(i->i.getArgument(0));
    }

    @Test void cn1AndCn6UpdateOnlyPreparedAndCannotOverwriteStatus() {
        Contract existing=contract("PREPARED"); Contract input=contract("ARCHIVED"); input.setName("Updated"); input.setFederalState("BY");
        when(calculations.vacationHours(existing)).thenReturn(80d);
        Contract updated=service.updatePrepared(existing,input);
        assertEquals("PREPARED",updated.getStatus()); assertEquals("Updated",updated.getName()); assertEquals("BY",updated.getFederalState());
        existing.setStatus("STARTED"); assertThrows(InvalidStateTransitionException.class,()->service.updatePrepared(existing,input));
    }

    @Test void cn7StartGeneratesWeeklyTimesheetsInProgress() {
        Contract c=contract("PREPARED"); c.setId(7L); c.setStartDate(LocalDate.of(2026,2,1)); c.setEndDate(LocalDate.of(2026,2,28)); c.setFrequency("WEEKLY");
        when(contracts.findById(7L)).thenReturn(Optional.of(c)); when(calculations.hoursDue(any(),any(),any())).thenReturn(20d);
        service.startContract(7L);
        assertEquals("STARTED",c.getStatus()); verify(sheets,times(5)).save(argThat(t->"IN_PROGRESS".equals(t.getStatus())&&t.getHoursDue()==20d));
    }

    @Test void cn9TerminationBlockedByEmployeeSignedSheet() {
        Contract c=contract("STARTED"); c.setId(8L); TimeSheet t=sheet(c,"SIGNED_BY_EMPLOYEE",false);
        when(contracts.findById(8L)).thenReturn(Optional.of(c)); when(sheets.findByContractIdOrderByPeriodStart(8L)).thenReturn(List.of(t));
        assertFalse(service.terminationPreview(8L).allowed()); assertThrows(InvalidStateTransitionException.class,()->service.terminateContract(8L));
    }

    @Test void cn10AndCn11PreviewWarnsThenTerminationRecordsDateAndDeletesOpenSheets() {
        Contract c=contract("STARTED"); c.setId(9L); TimeSheet entered=sheet(c,"IN_PROGRESS",true); TimeSheet approved=sheet(c,"SIGNED_BY_SUPERVISOR",false);
        when(contracts.findById(9L)).thenReturn(Optional.of(c)); when(sheets.findByContractIdOrderByPeriodStart(9L)).thenReturn(List.of(entered,approved));
        var preview=service.terminationPreview(9L); assertTrue(preview.allowed()); assertEquals(1,preview.enteredInProgressSheets()); assertTrue(preview.message().contains("contain entries"));
        service.terminateContract(9L); assertEquals("TERMINATED",c.getStatus()); assertEquals(LocalDate.now(),c.getTerminationDate()); verify(sheets).delete(entered); verify(sheets,never()).delete(approved);
    }

    @Test void cn3StatisticsReturnsDueReportedRemainingAndVacationBalances() {
        Contract c=contract("STARTED"); c.setId(10L); TimeSheet t=sheet(c,"IN_PROGRESS",false); t.setHoursDue(40); TimeEntry work=new TimeEntry(); work.setHours(8); work.setReportType("WORK"); TimeEntry leave=new TimeEntry(); leave.setHours(4); leave.setReportType("VACATION"); t.setEntries(List.of(work,leave));
        when(contracts.findById(10L)).thenReturn(Optional.of(c)); when(sheets.findByContractIdOrderByPeriodStart(10L)).thenReturn(List.of(t)); when(calculations.vacationHours(c)).thenReturn(16d);
        var stat=service.statistics(10L); assertEquals(40,stat.hoursDue()); assertEquals(12,stat.hoursReported()); assertEquals(28,stat.remainingHours()); assertEquals(16,stat.vacationHours()); assertEquals(4,stat.vacationHoursReported());
    }

    @Test void cn1ParticipantAccessRestrictsManagersToAssignedContracts() {
        AccessService access=new AccessService(); Contract c=contract("PREPARED"); User assistant=user("assistant",true); c.setAssistants(Set.of(assistant));
        assertTrue(access.contractManager(c,auth("assistant","ROLE_ASSISTANT"))); assertFalse(access.contractManager(c,auth("outsider","ROLE_ASSISTANT")));
    }

    private Contract contract(String status){ Contract c=new Contract(); c.setStatus(status); c.setName("Test"); c.setEmployee(user("employee",false)); c.setSupervisor(user("supervisor",true)); c.setStartDate(LocalDate.of(2026,1,1)); c.setEndDate(LocalDate.of(2026,12,31)); c.setFrequency("MONTHLY"); c.setWorkingHoursPerWeek(20); c.setWorkingDaysPerWeek(5); c.setVacationDaysPerYear(20); c.setFederalState("RP"); return c; }
    private TimeSheet sheet(Contract c,String status,boolean entered){ TimeSheet t=new TimeSheet(); t.setContract(c); t.setStatus(status); if(entered){ TimeEntry e=new TimeEntry(); e.setHours(2); e.setReportType("WORK"); t.setEntries(List.of(e)); } return t; }
    private User user(String name,boolean staff){ User u=new User(); u.setUsername(name); u.setUniversityStaff(staff); return u; }
    private UsernamePasswordAuthenticationToken auth(String name,String role){ return new UsernamePasswordAuthenticationToken(name,"",List.of(new SimpleGrantedAuthority(role))); }
}
