package org.tss.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@tss.local}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendReminderEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendEmployeeSubmissionReminder(String employeeEmail, String employeeName) {
        String subject = "TimeSheet Reminder - Please Submit Your TimeSheet";
        String body = "Dear " + employeeName + ",\n\n"
                + "This is a reminder to submit your timesheet for the current period.\n"
                + "Please log in to the system and complete your timesheet submission.\n\n"
                + "Best regards,\nTimeSheet Management System";
        sendReminderEmail(employeeEmail, subject, body);
    }

    public void sendSupervisorApprovalReminder(String supervisorEmail, String supervisorName, int pendingCount) {
        String subject = "TimeSheet Reminder - " + pendingCount + " TimeSheets Pending Approval";
        String body = "Dear " + supervisorName + ",\n\n"
                + "You have " + pendingCount + " timesheet(s) waiting for your approval.\n"
                + "Please log in to the system and review the pending timesheets.\n\n"
                + "Best regards,\nTimeSheet Management System";
        sendReminderEmail(supervisorEmail, subject, body);
    }

    public void sendArchivalReminder(String secretaryEmail, String secretaryName, int pendingCount) {
        String subject = "TimeSheet Reminder - " + pendingCount + " TimeSheets Ready for Archival";
        String body = "Dear " + secretaryName + ",\n\n"
                + "You have " + pendingCount + " timesheet(s) ready to be archived.\n"
                + "Please log in to the system and archive the completed timesheets.\n\n"
                + "Best regards,\nTimeSheet Management System";
        sendReminderEmail(secretaryEmail, subject, body);
    }
}
