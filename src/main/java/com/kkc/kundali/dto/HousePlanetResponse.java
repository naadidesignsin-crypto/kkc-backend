package com.kkc.kundali.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousePlanetResponse {

    private String name;
    private String rashi;
    private String nakshatra;
    private String degree;
    private Boolean retrograde;
    private Boolean combust;
}