package com.kkc.kundali.dto;

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
public class KundaliSummaryResponse {

    private Long id;

    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalTime timeOfBirth;
    private String birthPlace;
    private Double latitude;
    private Double longitude;
    private String timezone;

    private String provider;
    private KundaliReportStatus status;
    private String errorMessage;

    private String ascendant;
    private String rashi;
    private String signLord;

    private String nakshatra;
    private String nakshatraLord;
    private String charan;

    private String varna;
    private String vashya;
    private String yoni;
    private String gana;
    private String nadi;

    private String tithi;
    private String yoga;
    private String karan;
    private String masa;

    private String sunrise;
    private String sunset;

    private String tatva;
    private String nameAlphabetHindi;
    private String nameAlphabetEnglish;
    private String paya;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}