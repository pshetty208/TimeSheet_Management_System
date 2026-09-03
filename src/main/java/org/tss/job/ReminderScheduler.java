package org.tss.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tss.model.TimeSheet;
import org.tss.model.User;
import org.tss.repository.TimeSheetRepository;
import org.tss.repository.UserRepository;
import org.tss.service.EmailService;
import java.util.*;
import java.time.LocalDate;
import org.tss.repository.ContractRepository;

@Component
public class ReminderScheduler {

    private final TimeSheetRepository timeSheetRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ContractRepository contractRepository;

    public ReminderScheduler(TimeSheetRepository timeSheetRepository, UserRepository userRepository, EmailService emailService,
                             ContractRepository contractRepository) {
        this.timeSheetRepository = timeSheetRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.contractRepository = contractRepository;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendEmployeeReminders() {
        List<TimeSheet> inProgressSheets = timeSheetRepository.findByStatusAndPeriodEndLessThanEqual("IN_PROGRESS", LocalDate.now());
        Map<User, Integer> employeeTimeSheetCount = new HashMap<>();

        for (TimeSheet ts : inProgressSheets) {
            User employee = ts.getContract().getEmployee();
            employeeTimeSheetCount.merge(employee, 1, Integer::sum);
        }

        for (Map.Entry<User, Integer> entry : employeeTimeSheetCount.entrySet()) {
            User employee = entry.getKey();
            if (employee.getEmailAddress() != null) emailService.sendEmployeeSubmissionReminder(employee);
        }
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendSupervisorReminders() {
        List<TimeSheet> signedByEmployeeSheets = timeSheetRepository.findAllSignedByEmployee();
        Map<User, Integer> supervisorTimeSheetCount = new HashMap<>();

        for (TimeSheet ts : signedByEmployeeSheets) {
            supervisorTimeSheetCount.merge(ts.getContract().getSupervisor(), 1, Integer::sum);
            ts.getContract().getAssistants().forEach(a -> supervisorTimeSheetCount.merge(a, 1, Integer::sum));
        }

        for (Map.Entry<User, Integer> entry : supervisorTimeSheetCount.entrySet()) {
            User person = entry.getKey();
            if (person.getEmailAddress() != null) emailService.sendSupervisorApprovalReminder(person, entry.getValue());
        }
    }

    @Scheduled(cron = "0 0 11 * * *")
    public void sendArchivalReminders() {
        List<TimeSheet> signedByBothSheets = timeSheetRepository.findAllSignedBySupervisor();

        Map<User,Integer> recipients = new HashMap<>();
        signedByBothSheets.forEach(t -> t.getContract().getSecretaries().forEach(s -> recipients.merge(s, 1, Integer::sum)));
        recipients.forEach((person,count) -> { if (person.getEmailAddress() != null)
            emailService.sendArchivalReminder(person, count); });
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void purgeExpiredArchives() {
        for (TimeSheet sheet : timeSheetRepository.findByStatusAndSignedBySupervisorBefore("ARCHIVED", LocalDate.now())) {
            if (sheet.getSignedBySupervisor() != null && sheet.getSignedBySupervisor()
                    .plusMonths(sheet.getContract().getArchiveDurationMonths()).isBefore(LocalDate.now())) {
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
}
