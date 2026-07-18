package com.kkc.kundali.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkc.kundali.dto.KundaliGenerateRequest;
import com.kkc.kundali.dto.KundliApiProperties;
import com.kkc.kundali.dto.ProviderResult;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class KundliProviderClient {

    private final KundliApiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public KundliProviderClient(KundliApiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public ProviderResult generate(KundaliGenerateRequest request) {
        return callEndpoint(properties.getAstroDataPath(), request);
    }

    public ProviderResult callEndpoint(String endpointPath, KundaliGenerateRequest request) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("Kundli API key is not configured");
        }

        if (endpointPath == null || endpointPath.isBlank()) {
            throw new IllegalArgumentException("Provider endpoint path is required");
        }

        Map<String, Object> providerRequest = buildProviderRequest(request);

        try {
            JsonNode response = restClient.post()
                    .uri(endpointPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Api-Key", properties.getApiKey())
                    .body(providerRequest)
                    .retrieve()
                    .body(JsonNode.class);

            return ProviderResult.builder()
                    .requestJson(writeJson(providerRequest))
                    .responseJson(writeJson(response))
                    .build();

        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Kundli API failed: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(),
                    ex
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Kundli API failed: " + ex.getMessage(), ex);
        }
    }

    private Map<String, Object> buildProviderRequest(KundaliGenerateRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("day", request.getDateOfBirth().getDayOfMonth());
        body.put("month", request.getDateOfBirth().getMonthValue());
        body.put("year", request.getDateOfBirth().getYear());
        body.put("hour", request.getTimeOfBirth().getHour());
        body.put("min", request.getTimeOfBirth().getMinute());
        body.put("lat", request.getLatitude());
        body.put("lon", request.getLongitude());
        body.put("tzone", calculateTimezoneOffset(request));

        return body;
    }

    private double calculateTimezoneOffset(KundaliGenerateRequest request) {
        ZoneId zoneId = ZoneId.of(request.getTimezone());

        ZonedDateTime birthDateTime = ZonedDateTime.of(
                request.getDateOfBirth(),
                request.getTimeOfBirth(),
                zoneId
        );

        return birthDateTime.getOffset().getTotalSeconds() / 3600.0;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to serialize provider data", ex);
        }
    }
}
