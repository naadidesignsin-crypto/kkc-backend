package com.kkc.kundali.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KundaliPlanetsResponse {

    private Long reportId;
    private String sectionType;
    private String status;
    private List<PlanetPositionResponse> planets;
}