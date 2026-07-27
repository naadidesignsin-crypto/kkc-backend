package com.kkc.kundali.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record KundaliReportListItemResponse(
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
        String signLord,
        String nakshatra,
        String nakshatraLord,
        String currentDasha,
        LocalDateTime createdAt
) {
}