package com.kkc.kundali.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkc.kundali.dto.*;
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

    public KundaliSectionMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
                                    .name(text(planet, "name"))
                                    .degree(text(planet, "degree"))
                                    .latitude(doubleValue(planet, "latitude"))
                                    .longitude(doubleValue(planet, "longitude"))
                                    .rashi(text(planet, "rashi"))
                                    .rashiLord(text(planet, "rashiLord"))
                                    .nakshatra(text(planet, "nakshatra"))
                                    .nakshatraLord(text(planet, "nakshatraLord"))
                                    .charan(text(planet, "charan"))
                                    .house(intValue(planet, "house"))
                                    .retrograde(booleanValue(planet, "isRetrograde"))
                                    .combust(booleanValue(planet, "isCombust"))
                                    .planetState(text(planet, "PlanetState"))
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
                            .planet(text(dasha, "planet"))
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

            String type = text(mangalDosha, "type");
            String intensity = text(mangalDosha, "intensity");
            String reason = text(mangalDosha, "reason");
            String info = text(mangalDosha, "info");

            boolean present =
                    (type != null && !type.isBlank())
                            || (reason != null && reason.contains("मांगलिक"));

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

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        String textValue = value.asText();

        return textValue == null || textValue.isBlank() ? null : textValue;
    }

    private Double doubleValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        return value.asDouble();
    }

    private Integer intValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        return value.asInt();
    }

    private Boolean booleanValue(JsonNode node, String fieldName) {
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
}