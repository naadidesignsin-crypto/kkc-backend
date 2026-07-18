package com.kkc.kundali.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanetPositionResponse {

    private String name;
    private String degree;
    private Double latitude;
    private Double longitude;

    private String rashi;
    private String rashiLord;

    private String nakshatra;
    private String nakshatraLord;
    private String charan;

    private Integer house;
    private Boolean retrograde;
    private Boolean combust;
    private String planetState;
}