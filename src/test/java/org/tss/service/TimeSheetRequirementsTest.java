package org.tss.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.tss.controller.PrintController;
import org.tss.exception.UnauthorizedException;
import org.tss.exception.ValidationException;
import org.tss.model.Contract;
import org.tss.model.TimeEntry;
import org.tss.model.TimeSheet;
import org.tss.model.User;
import org.tss.repository.ContractRepository;
import org.tss.repository.TimeEntryRepository;
import org.tss.repository.TimeSheetRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TimeSheetRequirementsTest {

    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test void ts2EntriesRequireInProgressSheetAndStartedContract() {
        TimeSheetRepository sheets = mock(TimeSheetRepository.class);
        TimeEntryRepository entries = mock(TimeEntryRepository.class);
        WorkTimeCalculationService calculations = mock(WorkTimeCalculationService.class);
        AccessService access = new AccessService();
        TimeEntryService service = new TimeEntryService(entries, sheets, calculations, access);
        Contract contract = contract();
        TimeSheet sheet = sheet(contract, "IN_PROGRESS");
        when(sheets.findById(1L)).thenReturn(Optional.of(sheet));
        authenticate("employee", "ROLE_EMPLOYEE");

        TimeEntry entry = workEntry();
        assertSame(entry, service.addToTimesheet(1L, entry));
        verify(sheets).save(sheet);

        sheet.setStatus("SIGNED_BY_EMPLOYEE");
        assertThrows(ValidationException.class, () -> service.addToTimesheet(1L, workEntry()));
        sheet.setStatus("IN_PROGRESS");
        contract.setStatus("TERMINATED");
        assertThrows(ValidationException.class, () -> service.addToTimesheet(1L, workEntry()));
    }

    @Test void ts3VacationLimitAlsoAppliesWhenUpdatingEntries() {
        TimeSheetRepository sheets = mock(TimeSheetRepository.class);
        WorkTimeCalculationService calculations = mock(WorkTimeCalculationService.class);
        TimeEntryService service = new TimeEntryService(mock(TimeEntryRepository.class), sheets,
                calculations, new AccessService());
        Contract contract = contract();
        TimeSheet sheet = sheet(contract, "IN_PROGRESS");
        TimeEntry existingVacation = workEntry(); org.springframework.test.util.ReflectionTestUtils.setField(existingVacation, "id", 10L);
        existingVacation.setReportType("VACATION"); existingVacation.setHours(8);
        TimeEntry edited = workEntry(); org.springframework.test.util.ReflectionTestUtils.setField(edited, "id", 11L);
        sheet.setEntries(new java.util.ArrayList<>(List.of(existingVacation, edited)));
        when(sheets.findById(1L)).thenReturn(Optional.of(sheet));
        when(sheets.findByContractIdOrderByPeriodStart(1L)).thenReturn(List.of(sheet));
        when(calculations.vacationHours(contract)).thenReturn(8d);
        authenticate("employee", "ROLE_EMPLOYEE");

        TimeEntry replacement = workEntry(); replacement.setReportType("VACATION");
        assertThrows(ValidationException.class, () -> service.updateInTimesheet(1L, 11L, replacement));
        verify(sheets, never()).save(any());
    }

    @Test void ts9OnlyContractEmployeeCanManageEntries() {
        TimeSheetRepository sheets = mock(TimeSheetRepository.class);
        TimeEntryService service = new TimeEntryService(mock(TimeEntryRepository.class), sheets,
                mock(WorkTimeCalculationService.class), new AccessService());
        Contract contract = contract(); contract.setAssistants(Set.of(user("outsider")));
        when(sheets.findById(1L)).thenReturn(Optional.of(sheet(contract, "IN_PROGRESS")));
        authenticate("outsider", "ROLE_EMPLOYEE");

        assertThrows(UnauthorizedException.class, () -> service.addToTimesheet(1L, workEntry()));
        verify(sheets, never()).save(any());
    }

    @Test void ts4ToTs6TerminationDeletesOnlyInProgressSheets() {
        ContractRepository contracts = mock(ContractRepository.class);
        TimeSheetRepository sheets = mock(TimeSheetRepository.class);
        WorkTimeCalculationService calculations = mock(WorkTimeCalculationService.class);
        ContractService service = new ContractService(contracts, sheets, calculations);
        Contract contract = contract();
        TimeSheet open = sheet(contract, "IN_PROGRESS");
        TimeSheet approved = sheet(contract, "SIGNED_BY_SUPERVISOR");
        TimeSheet archived = sheet(contract, "ARCHIVED");
        when(contracts.findById(1L)).thenReturn(Optional.of(contract));
        when(contracts.save(any())).thenAnswer(call -> call.getArgument(0));
        when(sheets.findByContractIdOrderByPeriodStart(1L)).thenReturn(List.of(open, approved, archived));

        service.terminateContract(1L);

        verify(sheets).delete(open);
        verify(sheets, never()).delete(approved);
        verify(sheets, never()).delete(archived);
    }

    @Test void ts5EmployeeSignedSheetIsNeverDeletedDuringTermination() {
        ContractRepository contracts = mock(ContractRepository.class);
        TimeSheetRepository sheets = mock(TimeSheetRepository.class);
        ContractService service = new ContractService(contracts, sheets, mock(WorkTimeCalculationService.class));
        Contract contract = contract();
        TimeSheet employeeSigned = sheet(contract, "SIGNED_BY_EMPLOYEE");
        when(contracts.findById(1L)).thenReturn(Optional.of(contract));
        when(sheets.findByContractIdOrderByPeriodStart(1L)).thenReturn(List.of(employeeSigned));

        assertThrows(org.tss.exception.InvalidStateTransitionException.class, () -> service.terminateContract(1L));
        verify(sheets, never()).delete(any());
    }

    @Test void ts7AllAssignedRolesCanViewButOutsidersCannot() {
        AccessService access = new AccessService();
        Contract contract = contract();
        contract.setAssistants(Set.of(user("assistant")));
        contract.setSecretaries(Set.of(user("secretary")));
        TimeSheet sheet = sheet(contract, "IN_PROGRESS");

        assertTrue(access.timesheet(sheet, auth("employee", "ROLE_EMPLOYEE")));
        assertTrue(access.timesheet(sheet, auth("supervisor", "ROLE_SUPERVISOR")));
        assertTrue(access.timesheet(sheet, auth("assistant", "ROLE_ASSISTANT")));
        assertTrue(access.timesheet(sheet, auth("secretary", "ROLE_SECRETARY")));
        assertFalse(access.timesheet(sheet, auth("outsider", "ROLE_EMPLOYEE")));
    }

    @Test void ts8AssignedSecretaryCanProduceEscapedPrintableTimesheet() {
        TimeSheetRepository repository = mock(TimeSheetRepository.class);
        Contract contract = contract();
        contract.setSecretaries(Set.of(user("secretary")));
        TimeSheet sheet = sheet(contract, "SIGNED_BY_SUPERVISOR");
        TimeEntry entry = workEntry();
        entry.setDescription("Research <script>");
        sheet.setEntries(List.of(entry));
        when(repository.findById(1L)).thenReturn(Optional.of(sheet));
        PrintController controller = new PrintController(null,
                new TimeSheetService(repository, mock(ContractRepository.class)), new AccessService());

        String html = controller.sheet(1L, auth("secretary", "ROLE_SECRETARY"));
        assertTrue(html.contains("window.print") || html.contains("onclick=\"print()\""));
        assertTrue(html.contains("Research &lt;script&gt;"));
        assertThrows(UnauthorizedException.class,
                () -> controller.sheet(1L, auth("outsider", "ROLE_SECRETARY")));
    }

    private Contract contract() {
        Contract contract = new Contract();
        contract.setId(1L); contract.setStatus("STARTED");
        contract.setEmployee(user("employee")); contract.setSupervisor(user("supervisor"));
        return contract;
    }

    private TimeSheet sheet(Contract contract, String status) {
        TimeSheet sheet = new TimeSheet();
        sheet.setId(1L); sheet.setContract(contract); sheet.setStatus(status);
        sheet.setPeriodStart(LocalDate.of(2026, 1, 1)); sheet.setPeriodEnd(LocalDate.of(2026, 1, 31));
        return sheet;
    }

    private TimeEntry workEntry() {
        TimeEntry entry = new TimeEntry();
        entry.setDate(LocalDate.of(2026, 1, 2)); entry.setReportType("WORK");
        entry.setStartTime(LocalTime.of(9, 0)); entry.setEndTime(LocalTime.of(17, 0));
        return entry;
    }

    private User user(String username) { User user = new User(); user.setUsername(username); return user; }
    private void authenticate(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(auth(username, role));
    }
    private UsernamePasswordAuthenticationToken auth(String username, String role) {
        return new UsernamePasswordAuthenticationToken(username, "", List.of(new SimpleGrantedAuthority(role)));
    }
}
