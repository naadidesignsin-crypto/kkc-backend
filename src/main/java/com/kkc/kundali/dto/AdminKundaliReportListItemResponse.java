package com.kkc.kundali.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminKundaliReportListItemResponse(
        Long id,
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
        LocalDateTime createdAt
) {
}