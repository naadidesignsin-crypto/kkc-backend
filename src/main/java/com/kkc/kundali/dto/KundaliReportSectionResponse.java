package com.kkc.kundali.dto;

import com.kkc.kundali.entity.KundaliReportSection;
import com.kkc.kundali.util.KundaliReportSectionType;
import com.kkc.kundali.util.KundaliReportStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KundaliReportSectionResponse {

    private Long id;
    private Long reportId;
    private KundaliReportSectionType sectionType;
    private String provider;
    private String providerEndpoint;
    private KundaliReportStatus status;
    private String responseJson;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KundaliReportSectionResponse from(KundaliReportSection section) {
        return KundaliReportSectionResponse.builder()
                .id(section.getId())
                .reportId(section.getReportId())
                .sectionType(section.getSectionType())
                .provider(section.getProvider())
                .providerEndpoint(section.getProviderEndpoint())
                .status(section.getStatus())
                .responseJson(section.getResponseJson())
                .errorMessage(section.getErrorMessage())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}