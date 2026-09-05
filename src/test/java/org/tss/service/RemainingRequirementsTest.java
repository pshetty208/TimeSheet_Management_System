package org.tss.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.tss.job.ReminderScheduler;
import org.tss.model.Contract;
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

class RemainingRequirementsTest {
    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test void sg4CreatesAndVerifiesRsaSignatures() {
        TimeSheetRepository sheets = mock(TimeSheetRepository.class);
        ContractRepository contracts = mock(ContractRepository.class);
        TimeSheet sheet = sheet("IN_PROGRESS");
        when(sheets.findById(1L)).thenReturn(Optional.of(sheet));
        when(sheets.save(any())).thenAnswer(call -> call.getArgument(0));
        TimeSheetService service = new TimeSheetService(sheets, contracts, new DigitalSignatureService());

        authenticate("employee", "ROLE_EMPLOYEE");
        service.signByEmployee(1L);
        assertTrue(service.verifySignatures(1L).get("employee"));

        authenticate("supervisor", "ROLE_SUPERVISOR");
        service.signBySupervisor(1L);
        assertTrue(service.verifySignatures(1L).get("employee"));
        assertTrue(service.verifySignatures(1L).get("supervisor"));

        sheet.setHoursDue(99);
        assertFalse(service.verifySignatures(1L).get("employee"));
        assertFalse(service.verifySignatures(1L).get("supervisor"));
    }

    @Test void re5SendsOnlyOneCombinedEmailToMultiRoleUser() {
        TimeSheetRepository sheets = mock(TimeSheetRepository.class);
        EmailService emails = mock(EmailService.class);
        User person = user("person"); person.setEmailAddress("person@example.org");
        TimeSheet submission = sheet("IN_PROGRESS"); submission.getContract().setEmployee(person);
        TimeSheet approval = sheet("SIGNED_BY_EMPLOYEE"); approval.getContract().setSupervisor(person);
        TimeSheet archive = sheet("SIGNED_BY_SUPERVISOR"); archive.getContract().setSecretaries(Set.of(person));
        when(sheets.findByStatusAndPeriodEndLessThanEqual(eq("IN_PROGRESS"), any())).thenReturn(List.of(submission));
        when(sheets.findAllSignedByEmployee()).thenReturn(List.of(approval));
        when(sheets.findAllSignedBySupervisor()).thenReturn(List.of(archive));
        ReminderScheduler scheduler = new ReminderScheduler(sheets, emails, mock(ContractRepository.class));

        scheduler.sendDailyReminders();

        verify(emails, times(1)).sendDailyReminder(person, 1, 1, 1);
    }

    @Test void ar3DeletesOnTheExactRetentionDate() {
        TimeSheetRepository sheets = mock(TimeSheetRepository.class);
        ContractRepository contracts = mock(ContractRepository.class);
        TimeSheet archived = sheet("ARCHIVED");
        archived.setSignedBySupervisor(LocalDate.now().minusMonths(24));
        archived.getContract().setArchiveDurationMonths(24);
        when(sheets.findByStatusAndSignedBySupervisorBefore(eq("ARCHIVED"), any())).thenReturn(List.of(archived));
        when(sheets.findByContractIdOrderByPeriodStart(1L)).thenReturn(List.of());
        ReminderScheduler scheduler = new ReminderScheduler(sheets, mock(EmailService.class), contracts);

        scheduler.purgeExpiredArchives();

        verify(sheets).delete(archived);
        verify(contracts).delete(archived.getContract());
    }

    private TimeSheet sheet(String status) {
        Contract contract = new Contract(); contract.setId(1L); contract.setStatus("STARTED");
        contract.setEmployee(user("employee")); contract.setSupervisor(user("supervisor"));
        TimeSheet sheet = new TimeSheet(); sheet.setId(1L); sheet.setContract(contract); sheet.setStatus(status);
        sheet.setPeriodStart(LocalDate.of(2026, 1, 1)); sheet.setPeriodEnd(LocalDate.of(2026, 1, 31));
        sheet.setHoursDue(20);
        return sheet;
    }

    private User user(String username) { User user = new User(); user.setUsername(username); return user; }
    private void authenticate(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, "", List.of(new SimpleGrantedAuthority(role))));
    }
}
