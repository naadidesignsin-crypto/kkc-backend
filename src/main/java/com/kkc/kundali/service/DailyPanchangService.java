package com.kkc.kundali.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkc.kundali.dto.DailyPanchangResponse;
import com.kkc.kundali.dto.KundliApiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class DailyPanchangService {

    private static final String STYLE = "TELUGU_ANDHRA_TELANGANA";
    private static final String LANGUAGE = "te-en";
    private static final String DEFAULT_PLACE_KEY = "hyderabad";
    private static final String DEFAULT_PANCHANG_PATH = "/api/panchang/get_panchang_data";

    private final KundliApiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final Map<String, PanchangPlace> places;

    public DailyPanchangService(
            KundliApiProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(cleanBaseUrl(properties.getBaseUrl()))
                .build();
        this.places = buildPlaces();
    }

    public DailyPanchangResponse getDailyPanchang(
            LocalDate date,
            String place
    ) {
        LocalDate effectiveDate = date == null ? LocalDate.now() : date;
        PanchangPlace effectivePlace = resolvePlace(place);

        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new ResponseStatusException(
                    INTERNAL_SERVER_ERROR,
                    "Kundli API key is not configured. Set KUNDLI_API_KEY in backend environment."
            );
        }

        Map<String, Object> providerRequest = buildProviderRequest(
                effectiveDate,
                effectivePlace
        );

        JsonNode providerResponse = callKundliApi(providerRequest);

        return mapResponse(effectiveDate, effectivePlace, providerResponse);
    }

    private JsonNode callKundliApi(Map<String, Object> providerRequest) {
        String endpointPath = properties.getPanchangPath();

        if (endpointPath == null || endpointPath.isBlank()) {
            endpointPath = DEFAULT_PANCHANG_PATH;
        }

        try {
            return restClient.post()
                    .uri(endpointPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Api-Key", properties.getApiKey())
                    .body(providerRequest)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "Kundli Panchang API failed: "
                            + ex.getStatusCode()
                            + " - "
                            + ex.getResponseBodyAsString(),
                    ex
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "Kundli Panchang API failed: " + ex.getMessage(),
                    ex
            );
        }
    }

    private DailyPanchangResponse mapResponse(
            LocalDate date,
            PanchangPlace place,
            JsonNode response
    ) {
        return DailyPanchangResponse.builder()
                .date(date)
                .place(place.label())
                .cityKey(place.key())
                .latitude(place.latitude())
                .longitude(place.longitude())
                .timezone("Asia/Kolkata")
                .style(STYLE)
                .language(LANGUAGE)
                .varam(buildVaram(date.getDayOfWeek()))
                .tithi(read(response, "tithi", "Tithi", "tithiName", "tithi_name"))
                .nakshatram(read(response, "nakshatra", "Nakshatra", "nakshatram", "nakshatraName", "nakshatra_name"))
                .yogam(read(response, "yoga", "Yoga", "yogam", "yogaName", "yoga_name"))
                .karanam(read(response, "karana", "Karana", "karanam", "karanaName", "karana_name", "karan"))
                .paksham(read(response, "paksha", "Paksha", "paksham"))
                .masam(read(response, "masa", "Masa", "masam", "monthName", "hinduMonth", "hindu_month"))
                .samvatsaram(read(response, "samvat", "samvatsara", "samvatsaram", "vikramSamvat", "shakaSamvat"))
                .ayanam(read(response, "ayana", "ayanam", "ayan"))
                .ritu(read(response, "ritu", "season"))
                .sunrise(read(response, "sunrise", "sunRise", "sun_rise"))
                .sunset(read(response, "sunset", "sunSet", "sun_set"))
                .moonrise(read(response, "moonrise", "moonRise", "moon_rise"))
                .moonset(read(response, "moonset", "moonSet", "moon_set"))
                .rahuKalam(read(response, "rahuKaal", "rahuKalam", "rahukaal", "rahu_kaal", "rahu_kalam"))
                .yamagandam(read(response, "yamaganda", "yamagandam", "yamghantKaal", "yamghanta", "yamaghant"))
                .gulikaKalam(read(response, "gulikaKaal", "gulikaKalam", "gulika", "gulikKaal", "gulika_kaal"))
                .durmuhurtham(read(response, "durmuhurat", "durmuhurtham", "dur_muhurat", "durmuhurta"))
                .varjyam(read(response, "varjyam", "varja", "varjam"))
                .amritaKalam(read(response, "amritKaal", "amritaKalam", "amritKalam", "amrita_kaalam"))
                .abhijitMuhurtham(read(response, "abhijitMuhurta", "abhijitMuhurtham", "abhijit", "abhijit_muhurta"))
                .source("KundliAPI")
                .note("Daily Panchangam is generated for South Andhra & Telangana display style using selected date and place.")
                .generatedAt(LocalDateTime.now())
                .supportedPlaces(getSupportedPlaces())
                .build();
    }

    private Map<String, Object> buildProviderRequest(
            LocalDate date,
            PanchangPlace place
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("day", date.getDayOfMonth());
        body.put("month", date.getMonthValue());
        body.put("year", date.getYear());
        body.put("hour", 6);
        body.put("min", 0);
        body.put("lat", place.latitude());
        body.put("lon", place.longitude());
        body.put("tzone", 5.5);
        body.put("lang", "en");
        return body;
    }

    private PanchangPlace resolvePlace(String place) {
        if (place == null || place.isBlank()) {
            return places.get(DEFAULT_PLACE_KEY);
        }

        String key = cleanPlaceKey(place);
        PanchangPlace resolved = places.get(key);

        if (resolved != null) {
            return resolved;
        }

        PanchangPlace matchedByLabel = places.values().stream()
                .filter(item -> item.label().toLowerCase(Locale.ENGLISH)
                        .contains(place.trim().toLowerCase(Locale.ENGLISH)))
                .findFirst()
                .orElse(null);

        if (matchedByLabel != null) {
            return matchedByLabel;
        }

        throw new ResponseStatusException(
                BAD_REQUEST,
                "Unsupported Panchang place. Supported values: "
                        + String.join(", ", places.keySet())
        );
    }

    public List<DailyPanchangResponse.DailyPanchangPlaceOption> getSupportedPlaces() {
        return places.values().stream()
                .map(place -> DailyPanchangResponse.DailyPanchangPlaceOption.builder()
                        .key(place.key())
                        .label(place.label())
                        .state(place.state())
                        .build())
                .toList();
    }

    private Map<String, PanchangPlace> buildPlaces() {
        Map<String, PanchangPlace> map = new LinkedHashMap<>();

        addPlace(map, "hyderabad", "Hyderabad, Telangana", "Telangana", 17.3850, 78.4867);
        addPlace(map, "warangal", "Warangal, Telangana", "Telangana", 17.9689, 79.5941);
        addPlace(map, "vijayawada", "Vijayawada, Andhra Pradesh", "Andhra Pradesh", 16.5062, 80.6480);
        addPlace(map, "tirupati", "Tirupati, Andhra Pradesh", "Andhra Pradesh", 13.6288, 79.4192);
        addPlace(map, "visakhapatnam", "Visakhapatnam, Andhra Pradesh", "Andhra Pradesh", 17.6868, 83.2185);
        addPlace(map, "guntur", "Guntur, Andhra Pradesh", "Andhra Pradesh", 16.3067, 80.4365);
        addPlace(map, "rajahmundry", "Rajahmundry, Andhra Pradesh", "Andhra Pradesh", 17.0005, 81.8040);
        addPlace(map, "nellore", "Nellore, Andhra Pradesh", "Andhra Pradesh", 14.4426, 79.9865);

        return map;
    }

    private void addPlace(
            Map<String, PanchangPlace> map,
            String key,
            String label,
            String state,
            double latitude,
            double longitude
    ) {
        map.put(key, new PanchangPlace(key, label, state, latitude, longitude));
    }

    private String read(JsonNode root, String... aliases) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return null;
        }

        for (String alias : aliases) {
            JsonNode value = findFirst(root, alias);
            String formatted = formatValue(value);

            if (formatted != null && !formatted.isBlank()) {
                return formatted;
            }
        }

        return null;
    }

    private JsonNode findFirst(JsonNode node, String key) {
        if (node == null || key == null) {
            return null;
        }

        if (node.isObject()) {
            JsonNode direct = node.get(key);

            if (direct != null && !direct.isNull()) {
                return direct;
            }

            String normalizedKey = normalizeKey(key);
            var fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();

                if (normalizeKey(field.getKey()).equals(normalizedKey)
                        && field.getValue() != null
                        && !field.getValue().isNull()) {
                    return field.getValue();
                }
            }

            fields = node.fields();

            while (fields.hasNext()) {
                JsonNode found = findFirst(fields.next().getValue(), key);

                if (found != null && !found.isNull()) {
                    return found;
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = findFirst(child, key);

                if (found != null && !found.isNull()) {
                    return found;
                }
            }
        }

        return null;
    }

    private String formatValue(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }

        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return cleanText(node.asText());
        }

        if (node.isObject()) {
            String start = firstObjectValue(
                    node,
                    "start",
                    "startTime",
                    "start_time",
                    "from",
                    "begin",
                    "start_time_24"
            );
            String end = firstObjectValue(
                    node,
                    "end",
                    "endTime",
                    "end_time",
                    "to",
                    "finish",
                    "end_time_24"
            );

            if (start != null && end != null) {
                return start + " - " + end;
            }

            String name = firstObjectValue(
                    node,
                    "name",
                    "value",
                    "display",
                    "text"
            );

            if (name != null) {
                return name;
            }
        }

        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            return node.toString();
        }
    }

    private String firstObjectValue(JsonNode node, String... aliases) {
        for (String alias : aliases) {
            JsonNode value = findFirst(node, alias);

            if (value != null && value.isValueNode()) {
                String text = cleanText(value.asText());

                if (text != null) {
                    return text;
                }
            }
        }

        return null;
    }

    private String buildVaram(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "సోమవారం / Monday";
            case TUESDAY -> "మంగళవారం / Tuesday";
            case WEDNESDAY -> "బుధవారం / Wednesday";
            case THURSDAY -> "గురువారం / Thursday";
            case FRIDAY -> "శుక్రవారం / Friday";
            case SATURDAY -> "శనివారం / Saturday";
            case SUNDAY -> "ఆదివారం / Sunday";
        };
    }

    private String cleanBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://kundliapi.com";
        }

        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }

    private String cleanPlaceKey(String value) {
        return normalizeKey(value).replace(" ", "");
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .toLowerCase(Locale.ENGLISH)
                .replace("_", "")
                .replace("-", "")
                .replace(".", "")
                .replace("/", "")
                .replace(" ", "");
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }

        String clean = value.trim();
        return clean.isBlank() ? null : clean;
    }

    private record PanchangPlace(
            String key,
            String label,
            String state,
            double latitude,
            double longitude
    ) {
    }
}
