package com.kkc.kundali.dto;

public record DevDataResetResponse(
        boolean success,
        String message,
        int deletedSections,
        int deletedReports,
        boolean idsReset
) {
}