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
public class KundaliParasharaReportResponse {

    private Long reportId;
    private String sectionType;
    private String status;

    private String lagna;
    private String rashi;
    private String nakshatra;
    private String currentDasha;
    private String navamsaAscendant;

    private List<ParasharaSectionResponse> sections;
}