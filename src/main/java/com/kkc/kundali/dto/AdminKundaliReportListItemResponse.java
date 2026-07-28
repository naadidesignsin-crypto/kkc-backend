package com.kkc.kundali.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminKundaliReportListItemResponse(
        Long id,
        String orderId,
        String fullName,
        String gender,
        LocalDate dateOfBirth,
        LocalTime timeOfBirth,
        String birthPlace,
        String provider,
        String status,
        String ascendant,
        String rashi,
        String nakshatra,
        String currentDasha,
        Boolean showSummary,
        Boolean showConsultation,
        Boolean showBirthChart,
        Boolean showPlanets,
        Boolean showHouses,
        Boolean showNavamsa,
        Boolean showParashara,
        Boolean showDasha,
        Boolean showDosha,
        Boolean showPdf,
        LocalDateTime createdAt
) {
}