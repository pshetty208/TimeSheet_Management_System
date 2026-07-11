package org.tss.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timesheets")
public class TimeSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Contract contract;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private String status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeEntry> entries = new ArrayList<>();

    public TimeSheet() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<TimeEntry> getEntries() { return entries; }
    public void setEntries(List<TimeEntry> entries) { this.entries = entries; }
}