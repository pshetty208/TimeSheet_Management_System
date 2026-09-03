package org.tss.dto;

public record ContractTerminationPreview(long contractId, boolean allowed,
                                         int inProgressSheets, int enteredInProgressSheets,
                                         String message) {}
