package org.tss.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "contracts")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User employee;

    @ManyToOne
    private User supervisor;

    @ManyToMany
    private Set<User> assistants = new HashSet<>();

    @ManyToMany
    private Set<User> secretaries = new HashSet<>();

    private String name;

    private int workingHoursPerWeek;

    private LocalDate startDate;

    private LocalDate endDate;

    private String frequency;

    private double vacationEntitlement;

    private int workingDaysPerWeek = 5;
    private int vacationDaysPerYear = 20;
    private int archiveDurationMonths = 24;
    private String federalState = "RP";
    private LocalDate terminationDate;

    private String status;

    public Contract() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getEmployee() { return employee; }
    public void setEmployee(User employee) { this.employee = employee; }
    public User getSupervisor() { return supervisor; }
    public void setSupervisor(User supervisor) { this.supervisor = supervisor; }
    public int getWorkingHoursPerWeek() { return workingHoursPerWeek; }
    public void setWorkingHoursPerWeek(int workingHoursPerWeek) { this.workingHoursPerWeek = workingHoursPerWeek; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public double getVacationEntitlement() { return vacationEntitlement; }
    public void setVacationEntitlement(double vacationEntitlement) { this.vacationEntitlement = vacationEntitlement; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Set<User> getAssistants() { return assistants; }
    public void setAssistants(Set<User> assistants) { this.assistants = assistants; }
    public Set<User> getSecretaries() { return secretaries; }
    public void setSecretaries(Set<User> secretaries) { this.secretaries = secretaries; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getWorkingDaysPerWeek() { return workingDaysPerWeek; }
    public void setWorkingDaysPerWeek(int workingDaysPerWeek) { this.workingDaysPerWeek = workingDaysPerWeek; }
    public int getVacationDaysPerYear() { return vacationDaysPerYear; }
    public void setVacationDaysPerYear(int vacationDaysPerYear) { this.vacationDaysPerYear = vacationDaysPerYear; }
    public int getArchiveDurationMonths() { return archiveDurationMonths; }
    public void setArchiveDurationMonths(int archiveDurationMonths) { this.archiveDurationMonths = archiveDurationMonths; }
    public LocalDate getTerminationDate() { return terminationDate; }
    public void setTerminationDate(LocalDate terminationDate) { this.terminationDate = terminationDate; }
    public String getFederalState() { return federalState; }
    public void setFederalState(String federalState) { this.federalState = federalState; }
}
