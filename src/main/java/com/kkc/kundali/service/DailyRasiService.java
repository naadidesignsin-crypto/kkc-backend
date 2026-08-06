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

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class DailyRasiService {

    private static final String DEFAULT_PLACE_KEY = "hyderabad";
    private static final String DEFAULT_RASI_KEY = "mesha";
    private static final String DEFAULT_HOROSCOPE_PATH =
            "/api/horoscope/get_horoscope";

    private final KundliApiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final Map<String, RasiInfo> rasis;
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
        RasiInfo effectiveRasi = resolveRasi(rasi);
        RasiPlace effectivePlace = resolvePlace(place);

        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new ResponseStatusException(
                    INTERNAL_SERVER_ERROR,
                    "Kundli API key is not configured. Set KUNDLI_API_KEY in backend environment."
            );
        }

        JsonNode providerResponse = null;

        try {
            providerResponse = callKundliApi(
                    buildProviderRequest(effectiveDate, effectiveRasi, effectivePlace)
            );
        } catch (ResponseStatusException ex) {
            /*
             * Do not break homepage with raw provider failure.
             * We return a clean fallback response instead.
             */
        }

        return mapResponse(effectiveDate, effectiveRasi, effectivePlace, providerResponse);
    }

    private JsonNode callKundliApi(Map<String, Object> providerRequest) {
        String endpointPath = properties.getHoroscopePath();

        if (endpointPath == null || endpointPath.isBlank()) {
            endpointPath = DEFAULT_HOROSCOPE_PATH;
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
                    "Kundli Horoscope API failed: "
                            + ex.getStatusCode()
                            + " - "
                            + ex.getResponseBodyAsString(),
                    ex
            );
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "Kundli Horoscope API failed: " + ex.getMessage(),
                    ex
            );
        }
    }

    private DailyRasiResponse mapResponse(
            LocalDate date,
            RasiInfo rasi,
            RasiPlace place,
            JsonNode providerResponse
    ) {
        JsonNode responseForRasi = findRasiNode(providerResponse, rasi);

        if (responseForRasi == null) {
            responseForRasi = providerResponse;
        }

        DailyRasiResponse.DailyRasiSection daily = buildSection(
                responseForRasi,
                "Daily Prediction",
                List.of(
                        "daily",
                        "today",
                        "todayPrediction",
                        "dailyPrediction",
                        "daily_prediction",
                        "dailyHoroscope",
                        "daily_horoscope",
                        "prediction",
                        "overview",
                        "description"
                )
        );

        DailyRasiResponse.DailyRasiSection weekly = buildSection(
                responseForRasi,
                "Weekly Prediction",
                List.of(
                        "weekly",
                        "week",
                        "weeklyPrediction",
                        "weekly_prediction",
                        "weeklyHoroscope",
                        "weekly_horoscope"
                )
        );

        DailyRasiResponse.DailyRasiSection monthly = buildSection(
                responseForRasi,
                "Monthly Prediction",
                List.of(
                        "monthly",
                        "month",
                        "monthlyPrediction",
                        "monthly_prediction",
                        "monthlyHoroscope",
                        "monthly_horoscope"
                )
        );

        if (isEmptySection(daily)) {
            daily = fallbackSection(
                    "Daily Prediction",
                    rasi.displayName()
                            + " daily prediction is being prepared for this date."
            );
        }

        if (isEmptySection(weekly)) {
            weekly = fallbackSection(
                    "Weekly Prediction",
                    rasi.displayName()
                            + " weekly prediction is being prepared."
            );
        }

        if (isEmptySection(monthly)) {
            monthly = fallbackSection(
                    "Monthly Prediction",
                    rasi.displayName()
                            + " monthly prediction is being prepared."
            );
        }

        return DailyRasiResponse.builder()
                .date(date)
                .place(place.label())
                .cityKey(place.key())
                .rasiKey(rasi.key())
                .displayName(rasi.displayName())
                .teluguName(rasi.teluguName())
                .englishName(rasi.englishName())
                .zodiacName(rasi.zodiacName())
                .symbol(rasi.symbol())
                .language("te-en")
                .source(providerResponse == null ? "Fallback" : "KundliAPI")
                .note("Daily, weekly and monthly Rasi Phalalu are normalized from provider JSON into a clean customer-facing format.")
                .generatedAt(LocalDateTime.now())
                .daily(daily)
                .weekly(weekly)
                .monthly(monthly)
                .overview(daily.getOverview())
                .prediction(daily.getOverview())
                .career(daily.getCareer())
                .finance(daily.getFinance())
                .health(daily.getHealth())
                .family(daily.getFamily())
                .luckyColor(daily.getLuckyColor())
                .luckyNumber(daily.getLuckyNumber())
                .remedy(daily.getRemedy())
                .supportedRasis(getSupportedRasis())
                .supportedPlaces(getSupportedPlaces())
                .build();
    }

    private DailyRasiResponse.DailyRasiSection buildSection(
            JsonNode root,
            String title,
            List<String> sectionAliases
    ) {
        JsonNode sectionNode = null;

        if (root != null) {
            for (String alias : sectionAliases) {
                sectionNode = findFirst(root, alias);

                if (sectionNode != null && !sectionNode.isNull()) {
                    break;
                }
            }
        }

        if (sectionNode == null) {
            sectionNode = root;
        }

        String overview = firstCleanText(
                sectionNode,
                "overview",
                "prediction",
                "description",
                "content",
                "text",
                "summary",
                "result",
                "details",
                "horoscope"
        );

        String career = firstCleanText(
                sectionNode,
                "career",
                "profession",
                "job",
                "work",
                "business"
        );

        String finance = firstCleanText(
                sectionNode,
                "finance",
                "money",
                "wealth",
                "income"
        );

        String health = firstCleanText(
                sectionNode,
                "health",
                "wellness"
        );

        String family = firstCleanText(
                sectionNode,
                "family",
                "relationship",
                "relationships",
                "domestic",
                "home"
        );

        String love = firstCleanText(
                sectionNode,
                "love",
                "marriage",
                "romance",
                "partner"
        );

        String luckyColor = firstCleanText(
                sectionNode,
                "luckyColor",
                "lucky_color",
                "color",
                "lucky_colour",
                "luckyColour"
        );

        String luckyNumber = firstCleanText(
                sectionNode,
                "luckyNumber",
                "lucky_number",
                "number",
                "luckyNo",
                "lucky_no"
        );

        String remedy = firstCleanText(
                sectionNode,
                "remedy",
                "suggestion",
                "advice",
                "upay",
                "pariharam",
                "remedies"
        );

        String rawSummary = cleanNodeToText(sectionNode);

        if (overview == null || overview.isBlank()) {
            overview = rawSummary;
        }

        return DailyRasiResponse.DailyRasiSection.builder()
                .title(title)
                .overview(normalizeLongText(overview))
                .career(normalizeLongText(career))
                .finance(normalizeLongText(finance))
                .health(normalizeLongText(health))
                .family(normalizeLongText(family))
                .love(normalizeLongText(love))
                .luckyColor(normalizeShortText(luckyColor))
                .luckyNumber(normalizeShortText(luckyNumber))
                .remedy(normalizeLongText(remedy))
                .rawSummary(normalizeLongText(rawSummary))
                .build();
    }

    private DailyRasiResponse.DailyRasiSection fallbackSection(
            String title,
            String message
    ) {
        return DailyRasiResponse.DailyRasiSection.builder()
                .title(title)
                .overview(message)
                .rawSummary(message)
                .build();
    }

    private boolean isEmptySection(DailyRasiResponse.DailyRasiSection section) {
        return section == null
                || isBlank(section.getOverview())
                || looksLikeRawJson(section.getOverview());
    }

    private boolean looksLikeRawJson(String value) {
        if (value == null) {
            return false;
        }

        String clean = value.trim();

        return (clean.startsWith("{") && clean.endsWith("}"))
                || (clean.startsWith("[") && clean.endsWith("]"))
                || clean.contains("\":")
                || clean.contains("\\u003c")
                || clean.contains("&lt;");
    }

    private Map<String, Object> buildProviderRequest(
            LocalDate date,
            RasiInfo rasi,
            RasiPlace place
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("day", date.getDayOfMonth());
        body.put("month", date.getMonthValue());
        body.put("year", date.getYear());

        body.put("date", date.toString());
        body.put("rasi", rasi.key());
        body.put("sign", rasi.zodiacName());
        body.put("zodiac", rasi.zodiacName());
        body.put("moon_sign", rasi.zodiacName());

        body.put("place", place.label());
        body.put("lat", place.latitude());
        body.put("lon", place.longitude());
        body.put("tzone", 5.5);
        body.put("lang", "en");

        return body;
    }

    private JsonNode findRasiNode(JsonNode root, RasiInfo rasi) {
        if (root == null || root.isNull()) {
            return null;
        }

        List<String> aliases = List.of(
                rasi.key(),
                rasi.englishName(),
                rasi.zodiacName(),
                rasi.teluguName(),
                rasi.zodiacName().toLowerCase(Locale.ENGLISH)
        );

        for (String alias : aliases) {
            JsonNode found = findFirst(root, alias);

            if (found != null && !found.isNull() && found.isContainerNode()) {
                return found;
            }
        }

        return null;
    }

    private String firstCleanText(JsonNode root, String... aliases) {
        if (root == null || root.isNull()) {
            return null;
        }

        for (String alias : aliases) {
            JsonNode value = findFirst(root, alias);
            String text = cleanNodeToText(value);

            if (!isBlank(text) && !looksLikeRawJson(text)) {
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

    private String cleanNodeToText(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }

        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return cleanProviderText(node.asText());
        }

        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();

            for (JsonNode child : node) {
                String text = cleanNodeToText(child);

                if (!isBlank(text)) {
                    appendLine(builder, text);
                }
            }

            return builder.toString().trim();
        }

        if (node.isObject()) {
            StringBuilder builder = new StringBuilder();

            var fields = node.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = readableLabel(field.getKey());
                String fieldValue = cleanNodeToText(field.getValue());

                if (!isBlank(fieldValue) && !looksLikeTechnicalField(field.getKey())) {
                    appendLine(builder, fieldName + ": " + fieldValue);
                }
            }

            return builder.toString().trim();
        }

        try {
            return cleanProviderText(objectMapper.writeValueAsString(node));
        } catch (Exception ex) {
            return cleanProviderText(node.toString());
        }
    }

    private boolean looksLikeTechnicalField(String key) {
        String normalized = normalizeKey(key);

        return normalized.equals("id")
                || normalized.equals("uuid")
                || normalized.equals("status")
                || normalized.equals("code")
                || normalized.equals("success")
                || normalized.equals("error")
                || normalized.equals("errors")
                || normalized.equals("meta")
                || normalized.equals("metadata")
                || normalized.equals("request")
                || normalized.equals("response");
    }

    private String cleanProviderText(String value) {
        if (value == null) {
            return null;
        }

        String clean = value;

        clean = decodeUnicodeEscapes(clean);
        clean = decodeHtmlEntities(clean);

        clean = clean.replaceAll("(?i)<br\\s*/?>", "\n");
        clean = clean.replaceAll("(?i)</p>", "\n");
        clean = clean.replaceAll("(?i)</div>", "\n");
        clean = clean.replaceAll("(?i)</li>", "\n");
        clean = clean.replaceAll("(?i)<li>", "• ");
        clean = clean.replaceAll("<[^>]*>", " ");

        clean = clean.replace("\\n", "\n");
        clean = clean.replace("\\r", "\n");
        clean = clean.replace("\\t", " ");

        clean = clean.replace("{", " ");
        clean = clean.replace("}", " ");
        clean = clean.replace("[", " ");
        clean = clean.replace("]", " ");
        clean = clean.replace("\"", " ");

        clean = clean.replaceAll("(?m)^\\s*[,]+\\s*", "");
        clean = clean.replaceAll("\\s*:\\s*", ": ");
        clean = clean.replaceAll("[ \\t]{2,}", " ");
        clean = clean.replaceAll("\\n\\s+", "\n");
        clean = clean.replaceAll("\\s+\\n", "\n");
        clean = clean.replaceAll("\\n{3,}", "\n\n");

        clean = clean.trim();

        return clean.isBlank() ? null : clean;
    }

    private String normalizeLongText(String value) {
        String clean = cleanProviderText(value);

        if (clean == null) {
            return null;
        }

        if (looksLikeRawJson(clean)) {
            return null;
        }

        return clean;
    }

    private String normalizeShortText(String value) {
        String clean = normalizeLongText(value);

        if (clean == null) {
            return null;
        }

        if (clean.length() > 120) {
            return clean.substring(0, 120).trim();
        }

        return clean;
    }

    private String decodeHtmlEntities(String value) {
        return value
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'");
    }

    private String decodeUnicodeEscapes(String value) {
        String clean = value;

        clean = clean.replace("\\u003c", "<");
        clean = clean.replace("\\u003C", "<");
        clean = clean.replace("\\u003e", ">");
        clean = clean.replace("\\u003E", ">");
        clean = clean.replace("\\u0026", "&");
        clean = clean.replace("\\u0022", "\"");
        clean = clean.replace("\\u0027", "'");

        return clean;
    }

    private String readableLabel(String key) {
        if (key == null || key.isBlank()) {
            return "Details";
        }

        String spaced = key
                .replace("_", " ")
                .replace("-", " ")
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .trim();

        if (spaced.isBlank()) {
            return "Details";
        }

        return spaced.substring(0, 1).toUpperCase(Locale.ENGLISH)
                + spaced.substring(1);
    }

    private void appendLine(StringBuilder builder, String value) {
        if (builder.length() > 0) {
            builder.append("\n");
        }

        builder.append(value);
    }

    private RasiInfo resolveRasi(String rasi) {
        if (rasi == null || rasi.isBlank()) {
            return rasis.get(DEFAULT_RASI_KEY);
        }

        String key = cleanKey(rasi);
        RasiInfo resolved = rasis.get(key);

        if (resolved != null) {
            return resolved;
        }

        RasiInfo matched = rasis.values().stream()
                .filter(item -> normalizeKey(item.displayName()).contains(key)
                        || normalizeKey(item.englishName()).contains(key)
                        || normalizeKey(item.zodiacName()).contains(key)
                        || normalizeKey(item.teluguName()).contains(key))
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

        String key = cleanKey(place);
        RasiPlace resolved = places.get(key);

        if (resolved != null) {
            return resolved;
        }

        RasiPlace matched = places.values().stream()
                .filter(item -> normalizeKey(item.label()).contains(key))
                .findFirst()
                .orElse(null);

        if (matched != null) {
            return matched;
        }

        throw new ResponseStatusException(
                BAD_REQUEST,
                "Unsupported Rasi place. Supported values: "
                        + String.join(", ", places.keySet())
        );
    }

    public List<DailyRasiResponse.DailyRasiOption> getSupportedRasis() {
        return rasis.values().stream()
                .map(rasi -> DailyRasiResponse.DailyRasiOption.builder()
                        .key(rasi.key())
                        .teluguName(rasi.teluguName())
                        .englishName(rasi.englishName())
                        .zodiacName(rasi.zodiacName())
                        .symbol(rasi.symbol())
                        .build())
                .toList();
    }

    public List<DailyRasiResponse.DailyRasiPlaceOption> getSupportedPlaces() {
        return places.values().stream()
                .map(place -> DailyRasiResponse.DailyRasiPlaceOption.builder()
                        .key(place.key())
                        .label(place.label())
                        .state(place.state())
                        .build())
                .toList();
    }

    private Map<String, RasiInfo> buildRasis() {
        Map<String, RasiInfo> map = new LinkedHashMap<>();

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

        return map;
    }

    private void addRasi(
            Map<String, RasiInfo> map,
            String key,
            String teluguName,
            String englishName,
            String zodiacName,
            String symbol
    ) {
        RasiInfo info = new RasiInfo(
                key,
                teluguName,
                englishName,
                zodiacName,
                symbol,
                teluguName + " / " + englishName
        );

        map.put(key, info);
        map.put(cleanKey(englishName), info);
        map.put(cleanKey(zodiacName), info);
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String cleanBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://kundliapi.com";
        }

        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }

    private String cleanKey(String value) {
        return normalizeKey(value);
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

    private record RasiInfo(
            String key,
            String teluguName,
            String englishName,
            String zodiacName,
            String symbol,
            String displayName
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