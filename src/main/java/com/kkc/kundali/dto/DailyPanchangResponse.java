package com.kkc.kundali.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DailyPanchangResponse {

    private LocalDate date;
    private String place;
    private String cityKey;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String style;
    private String language;

    private String varam;
    private String tithi;
    private String nakshatram;
    private String yogam;
    private String karanam;
    private String paksham;
    private String masam;
    private String samvatsaram;
    private String ayanam;
    private String ritu;

    private String sunrise;
    private String sunset;
    private String moonrise;
    private String moonset;

    private String rahuKalam;
    private String yamagandam;
    private String gulikaKalam;
    private String durmuhurtham;
    private String varjyam;
    private String amritaKalam;
    private String abhijitMuhurtham;

    private String source;
    private String note;
    private LocalDateTime generatedAt;
    private List<DailyPanchangPlaceOption> supportedPlaces;

    @Getter
    @Builder
    public static class DailyPanchangPlaceOption {
        private String key;
        private String label;
        private String state;
    }
}
