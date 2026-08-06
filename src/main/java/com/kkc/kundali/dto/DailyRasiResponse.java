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

    private String rasiKey;
    private String displayName;
    private String teluguName;
    private String englishName;
    private String zodiacName;
    private String symbol;

    private String language;
    private String source;
    private String note;
    private LocalDateTime generatedAt;

    private DailyRasiSection daily;
    private DailyRasiSection weekly;
    private DailyRasiSection monthly;

    /*
     * Backward-compatible fields for older frontend code.
     * New frontend should use daily / weekly / monthly sections.
     */
    private String overview;
    private String prediction;
    private String career;
    private String finance;
    private String health;
    private String family;
    private String luckyColor;
    private String luckyNumber;
    private String remedy;

    private List<DailyRasiOption> supportedRasis;
    private List<DailyRasiPlaceOption> supportedPlaces;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRasiSection {
        private String title;
        private String overview;
        private String career;
        private String finance;
        private String health;
        private String family;
        private String love;
        private String luckyColor;
        private String luckyNumber;
        private String remedy;
        private String rawSummary;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRasiOption {
        private String key;
        private String teluguName;
        private String englishName;
        private String zodiacName;
        private String symbol;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyRasiPlaceOption {
        private String key;
        private String label;
        private String state;
    }
}