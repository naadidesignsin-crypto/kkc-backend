package com.kkc.kundali.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavamsaPlanetResponse {

    private String planetName;

    private String birthRashi;
    private Integer birthHouse;
    private Double birthLongitude;
    private String birthNakshatra;

    private Integer navamsaNumber;
    private String navamsaRashi;
    private Integer navamsaHouse;
}