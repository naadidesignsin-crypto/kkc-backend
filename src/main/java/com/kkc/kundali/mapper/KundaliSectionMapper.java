package com.kkc.kundali.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkc.kundali.dto.DashaPeriodResponse;
import com.kkc.kundali.dto.KundaliDashaResponse;
import com.kkc.kundali.dto.KundaliDoshaResponse;
import com.kkc.kundali.dto.KundaliPlanetsResponse;
import com.kkc.kundali.dto.PlanetPositionResponse;
import com.kkc.kundali.entity.KundaliReportSection;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class KundaliSectionMapper {

    private static final DateTimeFormatter DASHA_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH);

    private final ObjectMapper objectMapper;
    private final KundaliEnglishNormalizer englishNormalizer;

    public KundaliSectionMapper(
            ObjectMapper objectMapper,
            KundaliEnglishNormalizer englishNormalizer
    ) {
        this.objectMapper = objectMapper;
        this.englishNormalizer = englishNormalizer;
    }

    public KundaliPlanetsResponse toPlanetsResponse(KundaliReportSection section) {
        List<PlanetPositionResponse> planets = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(section.getResponseJson());

            JsonNode planetList = root
                    .path("responseData")
                    .path("data")
                    .path(0)
                    .path("planetData")
                    .path("planetList");

            if (planetList.isArray()) {
                for (JsonNode planet : planetList) {
                    planets.add(
                            PlanetPositionResponse.builder()
                                    .name(english(planet, "name"))
                                    .degree(text(planet, "degree"))
                                    .latitude(doubleValue(planet, "latitude"))
                                    .longitude(doubleValue(planet, "longitude"))
                                    .rashi(english(planet, "rashi"))
                                    .rashiLord(english(planet, "rashiLord"))
                                    .nakshatra(english(planet, "nakshatra"))
                                    .nakshatraLord(english(planet, "nakshatraLord"))
                                    .charan(text(planet, "charan"))
                                    .house(intValue(planet, "house"))
                                    .retrograde(booleanValue(planet, "isRetrograde"))
                                    .combust(booleanValue(planet, "isCombust"))
                                    .planetState(english(planet, "PlanetState"))
                                    .build()
                    );
                }
            }

        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse planetary positions response", ex);
        }

        return KundaliPlanetsResponse.builder()
                .reportId(section.getReportId())
                .sectionType(section.getSectionType().name())
                .status(section.getStatus().name())
                .planets(planets)
                .build();
    }

    public KundaliDashaResponse toDashaResponse(KundaliReportSection section) {
        List<DashaPeriodResponse> periods = new ArrayList<>();
        DashaPeriodResponse currentDasha = null;

        try {
            JsonNode root = objectMapper.readTree(section.getResponseJson());

            JsonNode dashaList = root
                    .path("responseData")
                    .path("data")
                    .path(0)
                    .path("vimshottaryMahaDashaData")
                    .path("vimshottaryMahaDashaList");

            LocalDateTime now = LocalDateTime.now();

            if (dashaList.isArray()) {
                for (JsonNode dasha : dashaList) {
                    String startDate = text(dasha, "startDate");
                    String endDate = text(dasha, "endDate");

                    boolean active = isDateActive(startDate, endDate, now);

                    DashaPeriodResponse response = DashaPeriodResponse.builder()
                            .planet(english(dasha, "planet"))
                            .startDate(startDate)
                            .endDate(endDate)
                            .active(active)
                            .build();

                    if (active) {
                        currentDasha = response;
                    }

                    periods.add(response);
                }
            }

        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse dasha response", ex);
        }

        return KundaliDashaResponse.builder()
                .reportId(section.getReportId())
                .sectionType(section.getSectionType().name())
                .status(section.getStatus().name())
                .currentDasha(currentDasha)
                .dashaPeriods(periods)
                .build();
    }

    public KundaliDoshaResponse toDoshaResponse(KundaliReportSection section) {
        try {
            JsonNode root = objectMapper.readTree(section.getResponseJson());

            JsonNode mangalDosha = root
                    .path("responseData")
                    .path("data")
                    .path(0)
                    .path("mangalDosha");

            String rawType = text(mangalDosha, "type");
            String rawIntensity = text(mangalDosha, "intensity");
            String rawReason = text(mangalDosha, "reason");
            String rawInfo = text(mangalDosha, "info");

            String type = englishNormalizer.normalize(rawType);
            String intensity = englishNormalizer.normalize(rawIntensity);
            String reason = englishNormalizer.normalize(rawReason);
            String info = englishNormalizer.normalize(rawInfo);

            boolean present = isMangalDoshaPresent(rawType, rawReason, type, reason);

            return KundaliDoshaResponse.builder()
                    .reportId(section.getReportId())
                    .sectionType(section.getSectionType().name())
                    .status(section.getStatus().name())
                    .mangalDoshaPresent(present)
                    .type(type)
                    .intensity(intensity)
                    .reason(reason)
                    .info(info)
                    .build();

        } catch (Exception ex) {
            throw new IllegalStateException("Unable to parse dosha response", ex);
        }
    }

    private boolean isMangalDoshaPresent(
            String rawType,
            String rawReason,
            String normalizedType,
            String normalizedReason
    ) {
        if (hasText(rawType) || hasText(normalizedType)) {
            return true;
        }

        String raw = rawReason == null ? "" : rawReason.toLowerCase(Locale.ENGLISH);
        String normalized = normalizedReason == null
                ? ""
                : normalizedReason.toLowerCase(Locale.ENGLISH);

        return raw.contains("मांगलिक")
                || raw.contains("मंगल दोष")
                || normalized.contains("mangal dosha is present")
                || normalized.contains("mangal dosha present")
                || normalized.contains("manglik");
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        String textValue = value.asText();

        return textValue == null || textValue.isBlank() ? null : textValue.trim();
    }

    private String english(JsonNode node, String fieldName) {
        return englishNormalizer.normalize(text(node, fieldName));
    }

    private Double doubleValue(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        return value.asDouble();
    }

    private Integer intValue(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        return value.asInt();
    }

    private Boolean booleanValue(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        return value.asBoolean();
    }

    private boolean isDateActive(String startDate, String endDate, LocalDateTime now) {
        try {
            if (startDate == null || endDate == null) {
                return false;
            }

            LocalDateTime start = LocalDateTime.parse(startDate, DASHA_FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endDate, DASHA_FORMATTER);

            return !now.isBefore(start) && !now.isAfter(end);

        } catch (Exception ex) {
            return false;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}