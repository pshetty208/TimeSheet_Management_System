package org.tss.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tss.model.TimeSheet;
import org.tss.model.User;
import org.tss.repository.TimeSheetRepository;
import org.tss.service.EmailService;
import java.util.*;
import java.time.LocalDate;
import org.tss.repository.ContractRepository;

@Component
public class ReminderScheduler {

    private final TimeSheetRepository timeSheetRepository;
    private final EmailService emailService;
    private final ContractRepository contractRepository;

    public ReminderScheduler(TimeSheetRepository timeSheetRepository, EmailService emailService,
                             ContractRepository contractRepository) {
        this.timeSheetRepository = timeSheetRepository;
        this.emailService = emailService;
        this.contractRepository = contractRepository;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyReminders() {
        Map<String, ReminderSummary> recipients = new LinkedHashMap<>();
        timeSheetRepository.findByStatusAndPeriodEndLessThanEqual("IN_PROGRESS", LocalDate.now()).forEach(sheet ->
                summary(recipients, sheet.getContract().getEmployee()).submissions++);
        timeSheetRepository.findAllSignedByEmployee().forEach(sheet -> {
            summary(recipients, sheet.getContract().getSupervisor()).approvals++;
            sheet.getContract().getAssistants().forEach(user -> summary(recipients, user).approvals++);
        });
        timeSheetRepository.findAllSignedBySupervisor().forEach(sheet ->
                sheet.getContract().getSecretaries().forEach(user -> summary(recipients, user).archives++));
        recipients.values().stream().filter(value -> value.user.getEmailAddress() != null)
                .forEach(value -> emailService.sendDailyReminder(value.user, value.submissions, value.approvals, value.archives));
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void purgeExpiredArchives() {
        for (TimeSheet sheet : timeSheetRepository.findByStatusAndSignedBySupervisorBefore("ARCHIVED", LocalDate.now())) {
            if (sheet.getSignedBySupervisor() != null && sheet.getSignedBySupervisor()
                    .plusMonths(sheet.getContract().getArchiveDurationMonths()).compareTo(LocalDate.now()) <= 0) {
                var contract = sheet.getContract();
                timeSheetRepository.delete(sheet);
                if (timeSheetRepository.findByContractIdOrderByPeriodStart(contract.getId()).isEmpty()) contractRepository.delete(contract);
            }
        }
    }

    @Scheduled(fixedDelay = 3600000)
    public void logReminderStats() {
        int inProgress = timeSheetRepository.findAllInProgress().size();
        int signedByEmployee = timeSheetRepository.findAllSignedByEmployee().size();
        int signedBySupervisor = timeSheetRepository.findAllSignedBySupervisor().size();

        System.out.println("TimeSheet Status Report:");
        System.out.println("  IN_PROGRESS: " + inProgress);
        System.out.println("  SIGNED_BY_EMPLOYEE: " + signedByEmployee);
        System.out.println("  SIGNED_BY_SUPERVISOR: " + signedBySupervisor);
    }

    private ReminderSummary summary(Map<String, ReminderSummary> recipients, User user) {
        return recipients.computeIfAbsent(user.getUsername(), key -> new ReminderSummary(user));
    }

    private static class ReminderSummary {
        private final User user;
        private int submissions;
        private int approvals;
        private int archives;

        private ReminderSummary(User user) { this.user = user; }
    }
}
