package com.kkc.kundali.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRasiResponse {

    private LocalDate date;
    private String place;
    private String cityKey;
    private Double latitude;
    private Double longitude;
    private String timezone;

    private String rasiKey;
    private String rasiTelugu;
    private String rasiEnglish;
    private String rasiSanskrit;
    private String symbol;

    private String style;
    private String language;

    private String overview;
    private String career;
    private String finance;
    private String health;
    private String familyAndRelationships;
    private String luckyColor;
    private String luckyNumber;
    private String remedy;
    private String source;
    private String note;

    private LocalDateTime generatedAt;
    private List<DailyRasiOption> supportedRasis;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRasiOption {
        private String key;
        private String telugu;
        private String english;
        private String sanskrit;
        private String symbol;
    }
}
