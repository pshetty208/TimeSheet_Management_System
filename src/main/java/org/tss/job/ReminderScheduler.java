package org.tss.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.tss.model.TimeSheet;
import org.tss.model.User;
import org.tss.repository.TimeSheetRepository;
import org.tss.repository.UserRepository;
import org.tss.service.EmailService;
import java.util.*;

@Component
public class ReminderScheduler {

    private final TimeSheetRepository timeSheetRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public ReminderScheduler(TimeSheetRepository timeSheetRepository, UserRepository userRepository, EmailService emailService) {
        this.timeSheetRepository = timeSheetRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 9 * * MON") // Every Monday at 9:00 AM
    public void sendEmployeeReminders() {
        List<TimeSheet> inProgressSheets = timeSheetRepository.findAllInProgress();
        Map<Long, Integer> employeeTimeSheetCount = new HashMap<>();

        for (TimeSheet ts : inProgressSheets) {
            Long employeeId = ts.getContract().getEmployee().getId();
            employeeTimeSheetCount.put(employeeId, employeeTimeSheetCount.getOrDefault(employeeId, 0) + 1);
        }

        for (Map.Entry<Long, Integer> entry : employeeTimeSheetCount.entrySet()) {
            Optional<User> employee = userRepository.findById(entry.getKey());
            if (employee.isPresent()) {
                System.out.println("Sending reminder to employee: " + employee.get().getUsername());
                // Note: Email sending would require actual email configuration
                // emailService.sendEmployeeSubmissionReminder(employeeEmail, employee.get().getUsername());
            }
        }
    }

    @Scheduled(cron = "0 10 * * MON") // Every Monday at 10:00 AM
    public void sendSupervisorReminders() {
        List<TimeSheet> signedByEmployeeSheets = timeSheetRepository.findAllSignedByEmployee();
        Map<Long, Integer> supervisorTimeSheetCount = new HashMap<>();

        for (TimeSheet ts : signedByEmployeeSheets) {
            Long supervisorId = ts.getContract().getSupervisor().getId();
            supervisorTimeSheetCount.put(supervisorId, supervisorTimeSheetCount.getOrDefault(supervisorId, 0) + 1);
        }

        for (Map.Entry<Long, Integer> entry : supervisorTimeSheetCount.entrySet()) {
            Optional<User> supervisor = userRepository.findById(entry.getKey());
            if (supervisor.isPresent()) {
                System.out.println("Sending approval reminder to supervisor: " + supervisor.get().getUsername() + " with " + entry.getValue() + " pending timesheets");
                // emailService.sendSupervisorApprovalReminder(supervisorEmail, supervisor.get().getUsername(), entry.getValue());
            }
        }
    }

    @Scheduled(cron = "0 11 * * MON") // Every Monday at 11:00 AM
    public void sendArchivalReminders() {
        List<TimeSheet> signedByBothSheets = timeSheetRepository.findAllSignedBySupervisor();

        if (!signedByBothSheets.isEmpty()) {
            System.out.println("Sending archival reminder for " + signedByBothSheets.size() + " timesheets");
            // In production, you'd fetch all secretaries and send individual reminders
            // emailService.sendArchivalReminder(secretaryEmail, secretaryName, signedByBothSheets.size());
        }
    }

    @Scheduled(fixedDelay = 3600000) // Every hour
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
