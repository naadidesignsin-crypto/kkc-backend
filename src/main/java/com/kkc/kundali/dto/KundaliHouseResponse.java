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
public class KundaliHouseResponse {

    private Long reportId;
    private String sectionType;
    private String status;
    private List<HouseInterpretationResponse> houses;
}