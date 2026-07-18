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
public class KundaliNavamsaResponse {

    private Long reportId;
    private String sectionType;
    private String status;

    private String navamsaAscendant;
    private List<NavamsaPlanetResponse> planets;
}