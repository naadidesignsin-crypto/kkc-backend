package com.kkc.location.service;

import com.kkc.location.dto.LocationSearchResponse;
import com.kkc.location.dto.NominatimPlaceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocationSearchService {

    private static final Duration CACHE_TTL = Duration.ofDays(30);
    private static final long MIN_REQUEST_GAP_MS = 1100L;

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String userAgent;
    private final String countryCodes;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private final Object rateLimitLock = new Object();
    private long lastExternalRequestTime = 0L;

    public LocationSearchService(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${location.search.base-url:https://nominatim.openstreetmap.org}") String baseUrl,
            @Value("${location.search.user-agent:KKC-Astrology/1.0}") String userAgent,
            @Value("${location.search.country-codes:in}") String countryCodes
    ) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();

        this.baseUrl = removeTrailingSlash(baseUrl);
        this.userAgent = userAgent;
        this.countryCodes = countryCodes;
    }

    public List<LocationSearchResponse> search(String query, int limit) {
        String cleanQuery = clean(query);

        if (cleanQuery.length() < 3) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, 10));
        String cacheKey = buildCacheKey(cleanQuery, safeLimit);

        CacheEntry cached = cache.get(cacheKey);

        if (cached != null && !cached.isExpired()) {
            return cached.results();
        }

        enforceRateLimit();

        List<LocationSearchResponse> results = callNominatim(cleanQuery, safeLimit);

        cache.put(cacheKey, new CacheEntry(results, Instant.now()));

        return results;
    }

    private List<LocationSearchResponse> callNominatim(String query, int limit) {
        String url = buildUrl(query, limit);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.USER_AGENT, userAgent);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<NominatimPlaceResponse[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NominatimPlaceResponse[].class
        );

        NominatimPlaceResponse[] places = response.getBody();

        if (places == null || places.length == 0) {
            return List.of();
        }

        return Arrays.stream(places)
                .map(this::toLocationSearchResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    private String buildUrl(String query, int limit) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/search")
                .queryParam("q", query)
                .queryParam("format", "jsonv2")
                .queryParam("addressdetails", "1")
                .queryParam("limit", limit)
                .queryParam("accept-language", "en");

        if (countryCodes != null && !countryCodes.isBlank()) {
            builder.queryParam("countrycodes", countryCodes.trim());
        }

        return builder.build()
                .encode()
                .toUriString();
    }

    private LocationSearchResponse toLocationSearchResponse(NominatimPlaceResponse place) {
        if (place == null) {
            return null;
        }

        Double latitude = parseDouble(place.getLat());
        Double longitude = parseDouble(place.getLon());

        if (latitude == null || longitude == null) {
            return null;
        }

        Map<String, String> address = place.getAddress() == null
                ? Map.of()
                : place.getAddress();

        String city = firstAvailable(
                address.get("city"),
                address.get("town"),
                address.get("village"),
                address.get("municipality"),
                address.get("suburb"),
                address.get("county")
        );

        String state = firstAvailable(
                address.get("state"),
                address.get("state_district")
        );

        String country = address.get("country");
        String countryCode = address.get("country_code");

        String birthPlace = buildBirthPlace(city, state, country, place.getDisplayName());
        String timezone = resolveTimezone(countryCode);

        return LocationSearchResponse.builder()
                .id(buildId(place))
                .displayName(safe(place.getDisplayName()))
                .birthPlace(birthPlace)
                .latitude(latitude)
                .longitude(longitude)
                .timezone(timezone)
                .city(safe(city))
                .state(safe(state))
                .country(safe(country))
                .countryCode(safe(countryCode))
                .source("OpenStreetMap Nominatim")
                .build();
    }

    private String buildBirthPlace(
            String city,
            String state,
            String country,
            String fallbackDisplayName
    ) {
        List<String> parts = new ArrayList<>();

        addIfPresent(parts, city);
        addIfPresent(parts, state);
        addIfPresent(parts, country);

        if (!parts.isEmpty()) {
            return String.join(", ", parts);
        }

        return safe(fallbackDisplayName);
    }

    private String buildId(NominatimPlaceResponse place) {
        if (place.getOsmType() != null && place.getOsmId() != null) {
            return place.getOsmType() + "-" + place.getOsmId();
        }

        if (place.getPlaceId() != null) {
            return "place-" + place.getPlaceId();
        }

        return UUID.randomUUID().toString();
    }

    private void enforceRateLimit() {
        synchronized (rateLimitLock) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastExternalRequestTime;

            if (elapsed < MIN_REQUEST_GAP_MS) {
                try {
                    Thread.sleep(MIN_REQUEST_GAP_MS - elapsed);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Location search was interrupted", ex);
                }
            }

            lastExternalRequestTime = System.currentTimeMillis();
        }
    }

    private String resolveTimezone(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return "Asia/Kolkata";
        }

        String cleanCountryCode = countryCode.trim().toLowerCase(Locale.ENGLISH);

        if ("in".equals(cleanCountryCode)) {
            return "Asia/Kolkata";
        }

        return "UTC";
    }

    private String buildCacheKey(String query, int limit) {
        return query.toLowerCase(Locale.ENGLISH).trim()
                + "|limit=" + limit
                + "|country=" + safe(countryCodes).toLowerCase(Locale.ENGLISH);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String removeTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://nominatim.openstreetmap.org";
        }

        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String firstAvailable(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private void addIfPresent(List<String> parts, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        String cleanValue = value.trim();

        if (!parts.contains(cleanValue)) {
            parts.add(cleanValue);
        }
    }

    private record CacheEntry(
            List<LocationSearchResponse> results,
            Instant createdAt
    ) {
        boolean isExpired() {
            return createdAt.plus(CACHE_TTL).isBefore(Instant.now());
        }
    }
}