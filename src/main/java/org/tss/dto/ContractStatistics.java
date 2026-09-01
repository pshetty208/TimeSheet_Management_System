package org.tss.dto;

public record ContractStatistics(long contractId, double hoursDue, double hoursReported,
                                 double remainingHours, double vacationHours,
                                 double vacationHoursReported) {}
