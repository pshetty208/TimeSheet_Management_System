package org.tss.model;

import jakarta.persistence.*;
import java.time.LocalDate;

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

    private int workingHoursPerWeek;

    private LocalDate startDate;

    private LocalDate endDate;

    private String frequency;

    private double vacationEntitlement;

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
}