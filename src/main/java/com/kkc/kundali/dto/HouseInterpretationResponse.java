package com.kkc.kundali.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseInterpretationResponse {

    private Integer houseNumber;
    private String houseName;
    private String mainArea;
    private String meaning;
    private String interpretation;
    private List<HousePlanetResponse> planets;
}