package com.kkc.kundali.dto;

public record AdminDeleteKundaliReportResponse(
        boolean success,
        String message,
        Long reportId,
        long deletedSections,
        long deletedReports
) {
}