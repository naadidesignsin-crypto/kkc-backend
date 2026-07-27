package com.kkc.kundali.dto;

import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.util.KundaliReportStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KundaliReportResponse {

    private Long id;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalTime timeOfBirth;
    private String birthPlace;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String language;
    private String provider;

    private String ascendant;
    private String rashi;
    private String signLord;
    private String nakshatra;
    private String nakshatraLord;
    private String currentDasha;

    private KundaliReportStatus status;
    private String providerResponseJson;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KundaliReportResponse from(KundaliReport report) {
        return KundaliReportResponse.builder()
                .id(report.getId())
                .fullName(report.getFullName())
                .gender(report.getGender())
                .dateOfBirth(report.getDateOfBirth())
                .timeOfBirth(report.getTimeOfBirth())
                .birthPlace(report.getBirthPlace())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .timezone(report.getTimezone())
                .language(report.getLanguage())
                .provider(report.getProvider())
                .ascendant(report.getAscendant())
                .rashi(report.getRashi())
                .signLord(report.getSignLord())
                .nakshatra(report.getNakshatra())
                .nakshatraLord(report.getNakshatraLord())
                .currentDasha(report.getCurrentDasha())
                .status(report.getStatus())
                .providerResponseJson(report.getProviderResponseJson())
                .errorMessage(report.getErrorMessage())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}