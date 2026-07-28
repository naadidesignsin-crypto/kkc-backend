package com.kkc.kundali.dto;

public record AdminKundaliReportApprovalRequest(
        Boolean showSummary,
        Boolean showConsultation,
        Boolean showBirthChart,
        Boolean showPlanets,
        Boolean showHouses,
        Boolean showNavamsa,
        Boolean showParashara,
        Boolean showDasha,
        Boolean showDosha,
        Boolean showPdf
) {
}