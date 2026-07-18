package com.kkc.kundali.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkc.kundali.dto.KundaliReport;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.util.KundaliReportStatus;
import org.springframework.stereotype.Component;

@Component
public class KundaliSummaryMapper {

    private final ObjectMapper objectMapper;

    public KundaliSummaryMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public KundaliSummaryResponse from(KundaliReport report) {
        KundaliSummaryResponse.KundaliSummaryResponseBuilder builder =
                KundaliSummaryResponse.builder()
                        .id(report.getId())
                        .fullName(report.getFullName())
                        .gender(report.getGender())
                        .dateOfBirth(report.getDateOfBirth())
                        .timeOfBirth(report.getTimeOfBirth())
                        .birthPlace(report.getBirthPlace())
                        .latitude(report.getLatitude())
                        .longitude(report.getLongitude())
                        .timezone(report.getTimezone())
                        .provider(report.getProvider())
                        .status(report.getStatus())
                        .errorMessage(report.getErrorMessage())
                        .createdAt(report.getCreatedAt())
                        .updatedAt(report.getUpdatedAt());

        if (report.getProviderResponseJson() == null || report.getProviderResponseJson().isBlank()) {
            return builder.build();
        }

        try {
            JsonNode root = objectMapper.readTree(report.getProviderResponseJson());

            JsonNode astroData = root
                    .path("responseData")
                    .path("data")
                    .path(0)
                    .path("astrodata");

            if (astroData.isMissingNode() || astroData.isNull()) {
                return builder.build();
            }

            return builder
                    .ascendant(text(astroData, "ascendant"))
                    .rashi(text(astroData, "sign"))
                    .signLord(text(astroData, "signLord"))

                    // Provider has typo: "naksahtra".
                    // Keep fallback for future corrected spelling also.
                    .nakshatra(text(astroData, "nakshatra", "naksahtra"))
                    .nakshatraLord(text(astroData, "nakshatraLord"))
                    .charan(text(astroData, "charan"))

                    .varna(text(astroData, "varna"))
                    .vashya(text(astroData, "vashya"))
                    .yoni(text(astroData, "yoni"))
                    .gana(text(astroData, "gana"))
                    .nadi(text(astroData, "nadi"))

                    .tithi(text(astroData.path("tithi"), "name"))
                    .yoga(text(astroData.path("yog"), "name"))
                    .karan(text(astroData.path("karan"), "name"))
                    .masa(text(astroData, "masa"))

                    .sunrise(text(astroData, "sunrise"))
                    .sunset(text(astroData, "sunset"))

                    .tatva(text(astroData, "tatva"))
                    .nameAlphabetHindi(text(astroData, "nameAlphabetHindi"))
                    .nameAlphabetEnglish(text(astroData, "nameAlphabetEnglish"))
                    .paya(text(astroData, "paya"))
                    .build();

        } catch (Exception ex) {
            return builder
                    .status(KundaliReportStatus.FAILED)
                    .errorMessage("Unable to parse Kundali provider response")
                    .build();
        }
    }

    private String text(JsonNode node, String... fieldNames) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        for (String fieldName : fieldNames) {
            JsonNode value = node.path(fieldName);

            if (!value.isMissingNode() && !value.isNull()) {
                String textValue = value.asText();

                if (textValue != null && !textValue.isBlank()) {
                    return textValue;
                }
            }
        }

        return null;
    }
}