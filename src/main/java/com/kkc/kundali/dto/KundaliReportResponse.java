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
    private String orderId;

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

    private Boolean showSummary;
    private Boolean showConsultation;
    private Boolean showBirthChart;
    private Boolean showPlanets;
    private Boolean showHouses;
    private Boolean showNavamsa;
    private Boolean showParashara;
    private Boolean showDasha;
    private Boolean showDosha;
    private Boolean showPdf;

    private KundaliReportStatus status;
    private String providerResponseJson;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KundaliReportResponse from(KundaliReport report) {
        return KundaliReportResponse.builder()
                .id(report.getId())
                .orderId(report.getOrderId())
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
                .showSummary(report.getShowSummary())
                .showConsultation(report.getShowConsultation())
                .showBirthChart(report.getShowBirthChart())
                .showPlanets(report.getShowPlanets())
                .showHouses(report.getShowHouses())
                .showNavamsa(report.getShowNavamsa())
                .showParashara(report.getShowParashara())
                .showDasha(report.getShowDasha())
                .showDosha(report.getShowDosha())
                .showPdf(report.getShowPdf())
                .status(report.getStatus())
                .providerResponseJson(report.getProviderResponseJson())
                .errorMessage(report.getErrorMessage())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}