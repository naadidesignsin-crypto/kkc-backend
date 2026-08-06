package com.kkc.kundali.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkc.kundali.dto.DailyRasiResponse;
import com.kkc.kundali.dto.KundliApiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class DailyRasiService {

    private static final String STYLE = "TELUGU_ANDHRA_TELANGANA";
    private static final String LANGUAGE = "te-en";
    private static final String DEFAULT_PLACE_KEY = "hyderabad";
    private static final String DEFAULT_RASI_KEY = "mesha";
    private static final String DEFAULT_HOROSCOPE_PATH = "/api/horoscope/get_horoscope";

    private final KundliApiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final Map<String, RasiOptionInternal> rasis;
    private final Map<String, RasiPlace> places;

    public DailyRasiService(
            KundliApiProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(cleanBaseUrl(properties.getBaseUrl()))
                .build();
        this.rasis = buildRasis();
        this.places = buildPlaces();
    }

    public DailyRasiResponse getDailyRasi(
            LocalDate date,
            String rasi,
            String place
    ) {
        LocalDate effectiveDate = date == null ? LocalDate.now() : date;
        RasiOptionInternal effectiveRasi = resolveRasi(rasi);
        RasiPlace effectivePlace = resolvePlace(place);

        JsonNode providerResponse = tryCallProvider(
                effectiveDate,
                effectiveRasi,
                effectivePlace
        );

        if (providerResponse != null) {
            DailyRasiResponse mapped = mapProviderResponse(
                    effectiveDate,
                    effectiveRasi,
                    effectivePlace,
                    providerResponse
            );

            if (hasMeaningfulProviderContent(mapped)) {
                return mapped;
            }
        }

        return fallbackResponse(effectiveDate, effectiveRasi, effectivePlace);
    }

    private JsonNode tryCallProvider(
            LocalDate date,
            RasiOptionInternal rasi,
            RasiPlace place
    ) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            return null;
        }

        String endpointPath = properties.getHoroscopePath();

        if (endpointPath == null || endpointPath.isBlank()) {
            endpointPath = DEFAULT_HOROSCOPE_PATH;
        }

        Map<String, Object> requestBody = buildProviderRequest(date, rasi, place);

        try {
            return restClient.post()
                    .uri(endpointPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Api-Key", properties.getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> buildProviderRequest(
            LocalDate date,
            RasiOptionInternal rasi,
            RasiPlace place
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("day", date.getDayOfMonth());
        body.put("month", date.getMonthValue());
        body.put("year", date.getYear());
        body.put("date", date.toString());
        body.put("rasi", rasi.key());
        body.put("rashi", rasi.english());
        body.put("sign", rasi.english());
        body.put("zodiac", rasi.english());
        body.put("place", place.label());
        body.put("lat", place.latitude());
        body.put("lon", place.longitude());
        body.put("tzone", 5.5);
        body.put("lang", "en");
        return body;
    }

    private DailyRasiResponse mapProviderResponse(
            LocalDate date,
            RasiOptionInternal rasi,
            RasiPlace place,
            JsonNode response
    ) {
        return DailyRasiResponse.builder()
                .date(date)
                .place(place.label())
                .cityKey(place.key())
                .latitude(place.latitude())
                .longitude(place.longitude())
                .timezone("Asia/Kolkata")
                .rasiKey(rasi.key())
                .rasiTelugu(rasi.telugu())
                .rasiEnglish(rasi.english())
                .rasiSanskrit(rasi.sanskrit())
                .symbol(rasi.symbol())
                .style(STYLE)
                .language(LANGUAGE)
                .overview(firstText(
                        response,
                        "overview",
                        "prediction",
                        "description",
                        "dailyPrediction",
                        "daily_prediction",
                        "horoscope",
                        "botResponse",
                        "bot_response",
                        "text",
                        "summary"
                ))
                .career(firstText(response, "career", "profession", "job", "work"))
                .finance(firstText(response, "finance", "money", "wealth", "income"))
                .health(firstText(response, "health", "wellness"))
                .familyAndRelationships(firstText(
                        response,
                        "family",
                        "relationship",
                        "relationships",
                        "love",
                        "marriage"
                ))
                .luckyColor(firstText(
                        response,
                        "luckyColor",
                        "lucky_color",
                        "color",
                        "colour"
                ))
                .luckyNumber(firstText(
                        response,
                        "luckyNumber",
                        "lucky_number",
                        "number"
                ))
                .remedy(firstText(
                        response,
                        "remedy",
                        "suggestion",
                        "advice",
                        "tip"
                ))
                .source("KundliAPI")
                .note("Daily Rasi Phalalu is requested from provider by selected date, rasi and place. If provider returns limited fields, unavailable sections are shown as '-'.")
                .generatedAt(LocalDateTime.now())
                .supportedRasis(getSupportedRasis())
                .build();
    }

    private boolean hasMeaningfulProviderContent(DailyRasiResponse response) {
        return hasText(response.getOverview())
                || hasText(response.getCareer())
                || hasText(response.getFinance())
                || hasText(response.getHealth())
                || hasText(response.getFamilyAndRelationships())
                || hasText(response.getRemedy());
    }

    private DailyRasiResponse fallbackResponse(
            LocalDate date,
            RasiOptionInternal rasi,
            RasiPlace place
    ) {
        return DailyRasiResponse.builder()
                .date(date)
                .place(place.label())
                .cityKey(place.key())
                .latitude(place.latitude())
                .longitude(place.longitude())
                .timezone("Asia/Kolkata")
                .rasiKey(rasi.key())
                .rasiTelugu(rasi.telugu())
                .rasiEnglish(rasi.english())
                .rasiSanskrit(rasi.sanskrit())
                .symbol(rasi.symbol())
                .style(STYLE)
                .language(LANGUAGE)
                .overview("Daily Rasi details for this date are being prepared. Configure a rasi-based daily horoscope provider endpoint to show live predictions here.")
                .career("-")
                .finance("-")
                .health("-")
                .familyAndRelationships("-")
                .luckyColor("-")
                .luckyNumber("-")
                .remedy("-")
                .source("KKC_FALLBACK")
                .note("Provider did not return a rasi-based daily prediction. KundliAPI path can be configured using KUNDLI_HOROSCOPE_PATH.")
                .generatedAt(LocalDateTime.now())
                .supportedRasis(getSupportedRasis())
                .build();
    }

    public List<DailyRasiResponse.DailyRasiOption> getSupportedRasis() {
        return rasis.values().stream()
                .map(rasi -> DailyRasiResponse.DailyRasiOption.builder()
                        .key(rasi.key())
                        .telugu(rasi.telugu())
                        .english(rasi.english())
                        .sanskrit(rasi.sanskrit())
                        .symbol(rasi.symbol())
                        .build())
                .toList();
    }

    private RasiOptionInternal resolveRasi(String value) {
        if (value == null || value.isBlank()) {
            return rasis.get(DEFAULT_RASI_KEY);
        }

        String key = normalizeKey(value);
        RasiOptionInternal direct = rasis.get(key);

        if (direct != null) {
            return direct;
        }

        RasiOptionInternal matched = rasis.values().stream()
                .filter(item -> normalizeKey(item.english()).equals(key)
                        || normalizeKey(item.sanskrit()).equals(key)
                        || normalizeKey(item.telugu()).equals(key))
                .findFirst()
                .orElse(null);

        if (matched != null) {
            return matched;
        }

        throw new ResponseStatusException(
                BAD_REQUEST,
                "Unsupported Rasi. Supported values: "
                        + String.join(", ", rasis.keySet())
        );
    }

    private RasiPlace resolvePlace(String place) {
        if (place == null || place.isBlank()) {
            return places.get(DEFAULT_PLACE_KEY);
        }

        String key = normalizeKey(place);
        RasiPlace resolved = places.get(key);

        if (resolved != null) {
            return resolved;
        }

        RasiPlace matchedByLabel = places.values().stream()
                .filter(item -> item.label().toLowerCase(Locale.ENGLISH)
                        .contains(place.trim().toLowerCase(Locale.ENGLISH)))
                .findFirst()
                .orElse(null);

        if (matchedByLabel != null) {
            return matchedByLabel;
        }

        throw new ResponseStatusException(
                BAD_REQUEST,
                "Unsupported Rasi place. Supported values: "
                        + String.join(", ", places.keySet())
        );
    }

    private Map<String, RasiOptionInternal> buildRasis() {
        Map<String, RasiOptionInternal> map = new LinkedHashMap<>();

        addRasi(map, "mesha", "మేషం", "Mesha", "Aries", "♈");
        addRasi(map, "vrishabha", "వృషభం", "Vrishabha", "Taurus", "♉");
        addRasi(map, "mithuna", "మిథునం", "Mithuna", "Gemini", "♊");
        addRasi(map, "karkataka", "కర్కాటకం", "Karkataka", "Cancer", "♋");
        addRasi(map, "simha", "సింహం", "Simha", "Leo", "♌");
        addRasi(map, "kanya", "కన్య", "Kanya", "Virgo", "♍");
        addRasi(map, "tula", "తుల", "Tula", "Libra", "♎");
        addRasi(map, "vrischika", "వృశ్చికం", "Vrischika", "Scorpio", "♏");
        addRasi(map, "dhanu", "ధనుస్సు", "Dhanu", "Sagittarius", "♐");
        addRasi(map, "makara", "మకరం", "Makara", "Capricorn", "♑");
        addRasi(map, "kumbha", "కుంభం", "Kumbha", "Aquarius", "♒");
        addRasi(map, "meena", "మీనం", "Meena", "Pisces", "♓");

        addAlias(map, "aries", "mesha");
        addAlias(map, "taurus", "vrishabha");
        addAlias(map, "gemini", "mithuna");
        addAlias(map, "cancer", "karkataka");
        addAlias(map, "leo", "simha");
        addAlias(map, "virgo", "kanya");
        addAlias(map, "libra", "tula");
        addAlias(map, "scorpio", "vrischika");
        addAlias(map, "sagittarius", "dhanu");
        addAlias(map, "capricorn", "makara");
        addAlias(map, "aquarius", "kumbha");
        addAlias(map, "pisces", "meena");

        addAlias(map, "vrushabha", "vrishabha");
        addAlias(map, "karkatakam", "karkataka");
        addAlias(map, "vrishchika", "vrischika");
        addAlias(map, "dhanus", "dhanu");
        addAlias(map, "meenam", "meena");

        return map;
    }

    private void addRasi(
            Map<String, RasiOptionInternal> map,
            String key,
            String telugu,
            String sanskrit,
            String english,
            String symbol
    ) {
        map.put(key, new RasiOptionInternal(
                key,
                telugu,
                english,
                sanskrit,
                symbol
        ));
    }

    private void addAlias(
            Map<String, RasiOptionInternal> map,
            String alias,
            String targetKey
    ) {
        RasiOptionInternal target = map.get(targetKey);

        if (target != null) {
            map.put(alias, target);
        }
    }

    private Map<String, RasiPlace> buildPlaces() {
        Map<String, RasiPlace> map = new LinkedHashMap<>();

        addPlace(map, "hyderabad", "Hyderabad, Telangana", "Telangana",
                17.3850, 78.4867);
        addPlace(map, "warangal", "Warangal, Telangana", "Telangana",
                17.9689, 79.5941);
        addPlace(map, "vijayawada", "Vijayawada, Andhra Pradesh",
                "Andhra Pradesh", 16.5062, 80.6480);
        addPlace(map, "tirupati", "Tirupati, Andhra Pradesh",
                "Andhra Pradesh", 13.6288, 79.4192);
        addPlace(map, "visakhapatnam", "Visakhapatnam, Andhra Pradesh",
                "Andhra Pradesh", 17.6868, 83.2185);
        addPlace(map, "guntur", "Guntur, Andhra Pradesh",
                "Andhra Pradesh", 16.3067, 80.4365);
        addPlace(map, "rajahmundry", "Rajahmundry, Andhra Pradesh",
                "Andhra Pradesh", 17.0005, 81.8040);
        addPlace(map, "nellore", "Nellore, Andhra Pradesh",
                "Andhra Pradesh", 14.4426, 79.9865);

        return map;
    }

    private void addPlace(
            Map<String, RasiPlace> map,
            String key,
            String label,
            String state,
            double latitude,
            double longitude
    ) {
        map.put(key, new RasiPlace(key, label, state, latitude, longitude));
    }

    private String firstText(JsonNode root, String... aliases) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return null;
        }

        for (String alias : aliases) {
            JsonNode value = findFirst(root, alias);
            String text = formatValue(value);

            if (hasText(text)) {
                return text;
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

        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();

            for (JsonNode child : node) {
                String text = formatValue(child);

                if (hasText(text)) {
                    if (!builder.isEmpty()) {
                        builder.append(" ");
                    }

                    builder.append(text);
                }
            }

            return builder.isEmpty() ? null : builder.toString();
        }

        if (node.isObject()) {
            String name = firstObjectValue(
                    node,
                    "name",
                    "value",
                    "display",
                    "text",
                    "description",
                    "prediction",
                    "content"
            );

            if (hasText(name)) {
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

                if (hasText(text)) {
                    return text;
                }
            }
        }

        return null;
    }

    private String cleanBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://kundliapi.com";
        }

        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank() && !"-".equals(value.trim());
    }

    private record RasiOptionInternal(
            String key,
            String telugu,
            String english,
            String sanskrit,
            String symbol
    ) {
    }

    private record RasiPlace(
            String key,
            String label,
            String state,
            double latitude,
            double longitude
    ) {
    }
}
