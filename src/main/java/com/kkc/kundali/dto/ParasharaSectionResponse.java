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
public class ParasharaSectionResponse {

    private String sectionKey;
    private String title;
    private String summary;
    private List<String> focusAreas;
    private List<String> observations;
    private String guidance;
    private String caution;
}