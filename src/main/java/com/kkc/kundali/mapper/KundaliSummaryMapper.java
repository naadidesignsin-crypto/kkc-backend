package com.kkc.kundali.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.util.KundaliReportStatus;
import org.springframework.stereotype.Component;

@Component
public class KundaliSummaryMapper {

    private final ObjectMapper objectMapper;
    private final KundaliEnglishNormalizer englishNormalizer;

    public KundaliSummaryMapper(
            ObjectMapper objectMapper,
            KundaliEnglishNormalizer englishNormalizer
    ) {
        this.objectMapper = objectMapper;
        this.englishNormalizer = englishNormalizer;
    }

    public KundaliSummaryResponse from(KundaliReport report) {
        KundaliSummaryResponse.KundaliSummaryResponseBuilder builder =
                KundaliSummaryResponse.builder()
                        .id(report.getId())
                        .fullName(report.getFullName())
                        .gender(englishNormalizer.normalize(report.getGender()))
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
                    .ascendant(english(astroData, "ascendant"))
                    .rashi(english(astroData, "sign"))
                    .signLord(english(astroData, "signLord"))

                    // Provider has typo: "naksahtra".
                    // Keep fallback for future corrected spelling also.
                    .nakshatra(english(astroData, "nakshatra", "naksahtra"))
                    .nakshatraLord(english(astroData, "nakshatraLord"))
                    .charan(text(astroData, "charan"))

                    .varna(english(astroData, "varna"))
                    .vashya(english(astroData, "vashya"))
                    .yoni(english(astroData, "yoni"))
                    .gana(english(astroData, "gana"))
                    .nadi(english(astroData, "nadi"))

                    .tithi(english(astroData.path("tithi"), "name"))
                    .yoga(english(astroData.path("yog"), "name"))
                    .karan(english(astroData.path("karan"), "name"))
                    .masa(english(astroData, "masa"))

                    .sunrise(text(astroData, "sunrise"))
                    .sunset(text(astroData, "sunset"))

                    .tatva(english(astroData, "tatva"))
                    .nameAlphabetHindi(english(astroData, "nameAlphabetHindi"))
                    .nameAlphabetEnglish(text(astroData, "nameAlphabetEnglish"))
                    .paya(english(astroData, "paya"))
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
                    return textValue.trim();
                }
            }
        }

        return null;
    }

    private String english(JsonNode node, String... fieldNames) {
        return englishNormalizer.normalize(text(node, fieldNames));
    }
}