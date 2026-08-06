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
    private static final String DEFAULT_PANCHANG_PATH =
            "/api/panchang/get_panchang_data";

    private static final Map<String, String> PAKSHAM = new LinkedHashMap<>();
    private static final Map<String, String> TITHI = new LinkedHashMap<>();
    private static final Map<String, String> NAKSHATRA = new LinkedHashMap<>();
    private static final Map<String, String> YOGA = new LinkedHashMap<>();
    private static final Map<String, String> KARANA = new LinkedHashMap<>();
    private static final Map<String, String> MASAM = new LinkedHashMap<>();
    private static final Map<String, String> SAMVATSARAM = new LinkedHashMap<>();
    private static final Map<String, String> AYANAM = new LinkedHashMap<>();
    private static final Map<String, String> RITU = new LinkedHashMap<>();

    static {
        loadPaksham();
        loadTithi();
        loadNakshatra();
        loadYoga();
        loadKarana();
        loadMasam();
        loadSamvatsaram();
        loadAyanam();
        loadRitu();
    }

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
        String rawTithi = read(
                response,
                "tithi",
                "Tithi",
                "tithiName",
                "tithi_name"
        );

        String rawNakshatra = read(
                response,
                "nakshatra",
                "Nakshatra",
                "nakshatram",
                "nakshatraName",
                "nakshatra_name"
        );

        String rawYoga = read(
                response,
                "yoga",
                "Yoga",
                "yogam",
                "yogaName",
                "yoga_name",
                "yog",
                "Yog"
        );

        String rawKarana = read(
                response,
                "karana",
                "Karana",
                "karanam",
                "karanaName",
                "karana_name",
                "karan",
                "Karan"
        );

        String rawPaksha = read(
                response,
                "paksha",
                "Paksha",
                "paksham",
                "paksh"
        );

        String rawMasam = read(
                response,
                "masa",
                "Masa",
                "masam",
                "monthName",
                "hinduMonth",
                "hindu_month",
                "hinduMaah",
                "hindu_maah",
                "maah"
        );

        String rawSamvatsaram = read(
                response,
                "samvatsaram",
                "samvatsara",
                "Samvatsaram",
                "Samvatsara",
                "samvatsaraName",
                "samvatsara_name",
                "samvatsaramName",
                "samvatsaram_name",
                "hinduYearName",
                "hindu_year_name",
                "yearName",
                "year_name",
                "samvat"
        );

        String rawAyanam = read(
                response,
                "ayanam",
                "ayana",
                "Ayanam",
                "Ayana",
                "ayan",
                "uttarayanaDakshinayana",
                "uttarayana_dakshinayana"
        );

        String rawRitu = read(
                response,
                "ritu",
                "Ritu",
                "season",
                "Season"
        );

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
                .tithi(formatTithi(rawTithi))
                .nakshatram(formatSingle(rawNakshatra, NAKSHATRA))
                .yogam(formatSingle(rawYoga, YOGA))
                .karanam(formatSingle(rawKarana, KARANA))
                .paksham(formatPaksham(rawPaksha, rawTithi))
                .masam(formatSingle(rawMasam, MASAM))
                .samvatsaram(formatSingle(rawSamvatsaram, SAMVATSARAM))
                .ayanam(formatAyanam(rawAyanam, date))
                .ritu(formatRitu(rawRitu, date))
                .sunrise(read(response, "sunrise", "sunRise", "sun_rise"))
                .sunset(read(response, "sunset", "sunSet", "sun_set"))
                .moonrise(read(response, "moonrise", "moonRise", "moon_rise"))
                .moonset(read(response, "moonset", "moonSet", "moon_set"))
                .rahuKalam(read(
                        response,
                        "rahuKaal",
                        "rahuKalam",
                        "rahukaal",
                        "rahu_kaal",
                        "rahu_kalam"
                ))
                .yamagandam(read(
                        response,
                        "yamaganda",
                        "yamagandam",
                        "yamghantKaal",
                        "yamghanta",
                        "yamaghant"
                ))
                .gulikaKalam(read(
                        response,
                        "gulikaKaal",
                        "gulikaKalam",
                        "gulika",
                        "gulikKaal",
                        "gulika_kaal"
                ))
                .durmuhurtham(read(
                        response,
                        "durmuhurat",
                        "durmuhurtham",
                        "dur_muhurat",
                        "durmuhurta"
                ))
                .varjyam(read(response, "varjyam", "varja", "varjam"))
                .amritaKalam(read(
                        response,
                        "amritKaal",
                        "amritaKalam",
                        "amritKalam",
                        "amrita_kaalam"
                ))
                .abhijitMuhurtham(read(
                        response,
                        "abhijitMuhurta",
                        "abhijitMuhurtham",
                        "abhijit",
                        "abhijit_muhurta"
                ))
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
                .map(place -> DailyPanchangResponse.DailyPanchangPlaceOption
                        .builder()
                        .key(place.key())
                        .label(place.label())
                        .state(place.state())
                        .build())
                .toList();
    }

    private Map<String, PanchangPlace> buildPlaces() {
        Map<String, PanchangPlace> map = new LinkedHashMap<>();

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
            Map<String, PanchangPlace> map,
            String key,
            String label,
            String state,
            double latitude,
            double longitude
    ) {
        map.put(
                key,
                new PanchangPlace(key, label, state, latitude, longitude)
        );
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

    private String formatTithi(String raw) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }

        String tithi = findMappedValue(raw, TITHI);
        String paksha = formatPaksham(null, raw);

        if (!"-".equals(paksha)) {
            return paksha + " - " + tithi;
        }

        return tithi;
    }

    private String formatPaksham(String rawPaksha, String fallbackText) {
        String raw = firstNotBlank(rawPaksha, fallbackText);

        if (raw == null) {
            return "-";
        }

        String lower = raw.toLowerCase(Locale.ENGLISH);

        if (containsAny(lower, "shukla", "sukla", "శుక్ల", "शुक्ल", "शुभ")) {
            return "శుక్ల పక్షం / Shukla Paksha";
        }

        if (containsAny(lower, "krishna", "krsna", "కృష్ణ", "कृष्ण")) {
            return "కృష్ణ పక్షం / Krishna Paksha";
        }

        return findMappedValue(raw, PAKSHAM);
    }

    private String formatSingle(String raw, Map<String, String> map) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }

        return findMappedValue(raw, map);
    }

    private String formatAyanam(String raw, LocalDate date) {
        String formatted = formatSingle(raw, AYANAM);

        if (!"-".equals(formatted)) {
            return formatted;
        }

        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        boolean uttarayana = month > 1 && month < 7;

        if (month == 1) {
            uttarayana = day >= 14;
        }

        if (month == 7) {
            uttarayana = day <= 16;
        }

        return uttarayana
                ? "ఉత్తరాయణం / Uttarayana"
                : "దక్షిణాయనం / Dakshinayana";
    }

    private String formatRitu(String raw, LocalDate date) {
        String formatted = formatSingle(raw, RITU);

        if (!"-".equals(formatted)) {
            return formatted;
        }

        return switch (date.getMonthValue()) {
            case 3, 4 -> "వసంత ఋతువు / Vasanta Ritu";
            case 5, 6 -> "గ్రీష్మ ఋతువు / Grishma Ritu";
            case 7, 8 -> "వర్ష ఋతువు / Varsha Ritu";
            case 9, 10 -> "శరదృతువు / Sharad Ritu";
            case 11, 12 -> "హేమంత ఋతువు / Hemanta Ritu";
            case 1, 2 -> "శిశిర ఋతువు / Shishira Ritu";
            default -> "-";
        };
    }

    private String findMappedValue(String raw, Map<String, String> map) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }

        String clean = raw.trim();
        String lower = clean.toLowerCase(Locale.ENGLISH);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ENGLISH);

            if (lower.equals(key)) {
                return entry.getValue();
            }
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ENGLISH);

            if (lower.contains(key)) {
                return entry.getValue();
            }
        }

        return clean;
    }

    private String firstNotBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        return null;
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle.toLowerCase(Locale.ENGLISH))) {
                return true;
            }
        }

        return false;
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

    private static void loadPaksham() {
        PAKSHAM.put("shukla", "శుక్ల పక్షం / Shukla Paksha");
        PAKSHAM.put("sukla", "శుక్ల పక్షం / Shukla Paksha");
        PAKSHAM.put("shukla paksha", "శుక్ల పక్షం / Shukla Paksha");
        PAKSHAM.put("శుక్ల", "శుక్ల పక్షం / Shukla Paksha");
        PAKSHAM.put("शुक्ल", "శుక్ల పక్షం / Shukla Paksha");
        PAKSHAM.put("शुभ", "శుక్ల పక్షం / Shukla Paksha");

        PAKSHAM.put("krishna", "కృష్ణ పక్షం / Krishna Paksha");
        PAKSHAM.put("krsna", "కృష్ణ పక్షం / Krishna Paksha");
        PAKSHAM.put("krishna paksha", "కృష్ణ పక్షం / Krishna Paksha");
        PAKSHAM.put("కృష్ణ", "కృష్ణ పక్షం / Krishna Paksha");
        PAKSHAM.put("कृष्ण", "కృష్ణ పక్షం / Krishna Paksha");
    }

    private static void loadTithi() {
        TITHI.put("pratipada", "పాడ్యమి / Pratipada");
        TITHI.put("prathama", "పాడ్యమి / Pratipada");
        TITHI.put("प्रतिपदा", "పాడ్యమి / Pratipada");

        TITHI.put("dwitiya", "విదియ / Dwitiya");
        TITHI.put("द्वितीया", "విదియ / Dwitiya");

        TITHI.put("tritiya", "తదియ / Tritiya");
        TITHI.put("तृतीया", "తదియ / Tritiya");

        TITHI.put("chaturthi", "చవితి / Chaturthi");
        TITHI.put("चतुर्थी", "చవితి / Chaturthi");

        TITHI.put("panchami", "పంచమి / Panchami");
        TITHI.put("पंचमी", "పంచమి / Panchami");

        TITHI.put("shashthi", "షష్ఠి / Shashthi");
        TITHI.put("षष्ठी", "షష్ఠి / Shashthi");

        TITHI.put("saptami", "సప్తమి / Saptami");
        TITHI.put("सप्तमी", "సప్తమి / Saptami");

        TITHI.put("ashtami", "అష్టమి / Ashtami");
        TITHI.put("अष्टमी", "అష్టమి / Ashtami");

        TITHI.put("navami", "నవమి / Navami");
        TITHI.put("नवमी", "నవమి / Navami");

        TITHI.put("dashami", "దశమి / Dashami");
        TITHI.put("दशमी", "దశమి / Dashami");

        TITHI.put("ekadashi", "ఏకాదశి / Ekadashi");
        TITHI.put("एकादशी", "ఏకాదశి / Ekadashi");

        TITHI.put("dwadashi", "ద్వాదశి / Dwadashi");
        TITHI.put("द्वादशी", "ద్వాదశి / Dwadashi");

        TITHI.put("trayodashi", "త్రయోదశి / Trayodashi");
        TITHI.put("त्रयोदशी", "త్రయోదశి / Trayodashi");

        TITHI.put("chaturdashi", "చతుర్దశి / Chaturdashi");
        TITHI.put("चतुर्दशी", "చతుర్దశి / Chaturdashi");

        TITHI.put("purnima", "పౌర్ణమి / Purnima");
        TITHI.put("पूर्णिमा", "పౌర్ణమి / Purnima");

        TITHI.put("amavasya", "అమావాస్య / Amavasya");
        TITHI.put("अमावस्या", "అమావాస్య / Amavasya");
    }

    private static void loadNakshatra() {
        NAKSHATRA.put("ashwini", "అశ్విని / Ashwini");
        NAKSHATRA.put("अश्विनी", "అశ్విని / Ashwini");

        NAKSHATRA.put("bharani", "భరణి / Bharani");
        NAKSHATRA.put("भरणी", "భరణి / Bharani");

        NAKSHATRA.put("krittika", "కృత్తిక / Krittika");
        NAKSHATRA.put("kritika", "కృత్తిక / Krittika");
        NAKSHATRA.put("कृत्तिका", "కృత్తిక / Krittika");

        NAKSHATRA.put("rohini", "రోహిణి / Rohini");
        NAKSHATRA.put("रोहिणी", "రోహిణి / Rohini");

        NAKSHATRA.put("mrigashira", "మృగశిర / Mrigashira");
        NAKSHATRA.put("mrigashirsha", "మృగశిర / Mrigashira");
        NAKSHATRA.put("मृगशिरा", "మృగశిర / Mrigashira");

        NAKSHATRA.put("ardra", "ఆరుద్ర / Ardra");
        NAKSHATRA.put("आर्द्रा", "ఆరుద్ర / Ardra");

        NAKSHATRA.put("punarvasu", "పునర్వసు / Punarvasu");
        NAKSHATRA.put("पुनर्वसु", "పునర్వసు / Punarvasu");

        NAKSHATRA.put("pushya", "పుష్యమి / Pushya");
        NAKSHATRA.put("पुष्य", "పుష్యమి / Pushya");

        NAKSHATRA.put("ashlesha", "ఆశ్లేష / Ashlesha");
        NAKSHATRA.put("आश्लेषा", "ఆశ్లేష / Ashlesha");

        NAKSHATRA.put("magha", "మఖ / Magha");
        NAKSHATRA.put("मघा", "మఖ / Magha");

        NAKSHATRA.put("purva phalguni", "పూర్వ ఫల్గుణి / Purva Phalguni");
        NAKSHATRA.put("पूर्वाफाल्गुनी", "పూర్వ ఫల్గుణి / Purva Phalguni");

        NAKSHATRA.put("uttara phalguni", "ఉత్తర ఫల్గుణి / Uttara Phalguni");
        NAKSHATRA.put("उत्तराफाल्गुनी", "ఉత్తర ఫల్గుణి / Uttara Phalguni");

        NAKSHATRA.put("hasta", "హస్త / Hasta");
        NAKSHATRA.put("हस्त", "హస్త / Hasta");

        NAKSHATRA.put("chitra", "చిత్త / Chitra");
        NAKSHATRA.put("चित्रा", "చిత్త / Chitra");

        NAKSHATRA.put("swati", "స్వాతి / Swati");
        NAKSHATRA.put("स्वाती", "స్వాతి / Swati");

        NAKSHATRA.put("vishakha", "విశాఖ / Vishakha");
        NAKSHATRA.put("विशाखा", "విశాఖ / Vishakha");

        NAKSHATRA.put("anuradha", "అనూరాధ / Anuradha");
        NAKSHATRA.put("अनुराधा", "అనూరాధ / Anuradha");

        NAKSHATRA.put("jyeshtha", "జ్యేష్ఠ / Jyeshtha");
        NAKSHATRA.put("ज्येष्ठा", "జ్యేష్ఠ / Jyeshtha");

        NAKSHATRA.put("moola", "మూల / Moola");
        NAKSHATRA.put("mula", "మూల / Moola");
        NAKSHATRA.put("मूल", "మూల / Moola");

        NAKSHATRA.put("purva ashadha", "పూర్వాషాఢ / Purva Ashadha");
        NAKSHATRA.put("पूर्वाषाढ़ा", "పూర్వాషాఢ / Purva Ashadha");

        NAKSHATRA.put("uttara ashadha", "ఉత్తరాషాఢ / Uttara Ashadha");
        NAKSHATRA.put("उत्तराषाढ़ा", "ఉత్తరాషాఢ / Uttara Ashadha");

        NAKSHATRA.put("shravana", "శ్రవణం / Shravana");
        NAKSHATRA.put("श्रवण", "శ్రవణం / Shravana");

        NAKSHATRA.put("dhanishta", "ధనిష్ఠ / Dhanishta");
        NAKSHATRA.put("धनिष्ठा", "ధనిష్ఠ / Dhanishta");

        NAKSHATRA.put("shatabhisha", "శతభిషం / Shatabhisha");
        NAKSHATRA.put("शतभिषा", "శతభిషం / Shatabhisha");

        NAKSHATRA.put("purva bhadrapada", "పూర్వాభాద్ర / Purva Bhadrapada");
        NAKSHATRA.put("पूर्वभाद्रपदा", "పూర్వాభాద్ర / Purva Bhadrapada");

        NAKSHATRA.put("uttara bhadrapada", "ఉత్తరాభాద్ర / Uttara Bhadrapada");
        NAKSHATRA.put("उत्तरभाद्रपदा", "ఉత్తరాభాద్ర / Uttara Bhadrapada");

        NAKSHATRA.put("revati", "రేవతి / Revati");
        NAKSHATRA.put("रेवती", "రేవతి / Revati");
    }

    private static void loadYoga() {
        YOGA.put("vishkambha", "విష్కంభ / Vishkambha");
        YOGA.put("विष्कम्भ", "విష్కంభ / Vishkambha");

        YOGA.put("priti", "ప్రీతి / Priti");
        YOGA.put("preeti", "ప్రీతి / Priti");
        YOGA.put("प्रीति", "ప్రీతి / Priti");

        YOGA.put("ayushman", "ఆయుష్మాన్ / Ayushman");
        YOGA.put("आयुष्मान", "ఆయుష్మాన్ / Ayushman");

        YOGA.put("saubhagya", "సౌభాగ్య / Saubhagya");
        YOGA.put("सौभाग्य", "సౌభాగ్య / Saubhagya");

        YOGA.put("shobhana", "శోభన / Shobhana");
        YOGA.put("शोभन", "శోభన / Shobhana");

        YOGA.put("atiganda", "అతిగండ / Atiganda");
        YOGA.put("अतिगण्ड", "అతిగండ / Atiganda");

        YOGA.put("sukarma", "సుకర్మ / Sukarma");
        YOGA.put("सुकर्मा", "సుకర్మ / Sukarma");

        YOGA.put("dhriti", "ధృతి / Dhriti");
        YOGA.put("धृति", "ధృతి / Dhriti");

        YOGA.put("shoola", "శూల / Shoola");
        YOGA.put("shula", "శూల / Shoola");
        YOGA.put("शूल", "శూల / Shoola");

        YOGA.put("ganda", "గండ / Ganda");
        YOGA.put("गण्ड", "గండ / Ganda");

        YOGA.put("vriddhi", "వృద్ధి / Vriddhi");
        YOGA.put("वृद्धि", "వృద్ధి / Vriddhi");

        YOGA.put("dhruva", "ధృవ / Dhruva");
        YOGA.put("ध्रुव", "ధృవ / Dhruva");

        YOGA.put("vyaghata", "వ్యాఘాత / Vyaghata");
        YOGA.put("व्याघात", "వ్యాఘాత / Vyaghata");

        YOGA.put("harshana", "హర్షణ / Harshana");
        YOGA.put("हर्षण", "హర్షణ / Harshana");

        YOGA.put("vajra", "వజ్ర / Vajra");
        YOGA.put("वज्र", "వజ్ర / Vajra");

        YOGA.put("siddhi", "సిద్ధి / Siddhi");
        YOGA.put("सिद्धि", "సిద్ధి / Siddhi");

        YOGA.put("vyatipata", "వ్యతిపాత / Vyatipata");
        YOGA.put("व्यतीपात", "వ్యతిపాత / Vyatipata");

        YOGA.put("variyaan", "వరీయాన్ / Variyan");
        YOGA.put("variyan", "వరీయాన్ / Variyan");
        YOGA.put("वरीयान", "వరీయాన్ / Variyan");

        YOGA.put("parigha", "పరిఘ / Parigha");
        YOGA.put("परिघ", "పరిఘ / Parigha");

        YOGA.put("shiva", "శివ / Shiva");
        YOGA.put("शिव", "శివ / Shiva");

        YOGA.put("siddha", "సిద్ధ / Siddha");
        YOGA.put("सिद्ध", "సిద్ధ / Siddha");

        YOGA.put("sadhya", "సాధ్య / Sadhya");
        YOGA.put("साध्य", "సాధ్య / Sadhya");

        YOGA.put("shubha", "శుభ / Shubha");
        YOGA.put("शुभ", "శుభ / Shubha");

        YOGA.put("shukla", "శుక్ల / Shukla");
        YOGA.put("शुक्ल", "శుక్ల / Shukla");

        YOGA.put("brahma", "బ్రహ్మ / Brahma");
        YOGA.put("ब्रह्म", "బ్రహ్మ / Brahma");

        YOGA.put("indra", "ఇంద్ర / Indra");
        YOGA.put("इन्द्र", "ఇంద్ర / Indra");

        YOGA.put("vaidhrti", "వైధృతి / Vaidhriti");
        YOGA.put("vaidhriti", "వైధృతి / Vaidhriti");
        YOGA.put("वैधृति", "వైధృతి / Vaidhriti");
    }

    private static void loadKarana() {
        KARANA.put("bava", "బవ / Bava");
        KARANA.put("बव", "బవ / Bava");

        KARANA.put("balava", "బాలవ / Balava");
        KARANA.put("बालव", "బాలవ / Balava");

        KARANA.put("kaulava", "కౌలవ / Kaulava");
        KARANA.put("कौलव", "కౌలవ / Kaulava");

        KARANA.put("taitila", "తైతిల / Taitila");
        KARANA.put("तैतिल", "తైతిల / Taitila");

        KARANA.put("gara", "గర / Gara");
        KARANA.put("गर", "గర / Gara");

        KARANA.put("vanija", "వణిజ / Vanija");
        KARANA.put("वणिज", "వణిజ / Vanija");

        KARANA.put("vishti", "విష్టి / Vishti");
        KARANA.put("విష్టి", "విష్టి / Vishti");
        KARANA.put("विष्टि", "విష్టి / Vishti");

        KARANA.put("shakuni", "శకుని / Shakuni");
        KARANA.put("शकुनि", "శకుని / Shakuni");

        KARANA.put("chatushpada", "చతుష్పాద / Chatushpada");
        KARANA.put("chatushpad", "చతుష్పాద / Chatushpada");
        KARANA.put("चतुष्पद", "చతుష్పాద / Chatushpada");

        KARANA.put("naga", "నాగ / Naga");
        KARANA.put("नाग", "నాగ / Naga");

        KARANA.put("kimstughna", "కింస్తుఘ్న / Kimstughna");
        KARANA.put("किंस्तुघ्न", "కింస్తుఘ్న / Kimstughna");
    }

    private static void loadMasam() {
        MASAM.put("chaitra", "చైత్రం / Chaitra");
        MASAM.put("चैत्र", "చైత్రం / Chaitra");

        MASAM.put("vaishakha", "వైశాఖం / Vaishakha");
        MASAM.put("vaisakha", "వైశాఖం / Vaishakha");
        MASAM.put("वैशाख", "వైశాఖం / Vaishakha");

        MASAM.put("jyeshtha", "జ్యేష్ఠం / Jyeshtha");
        MASAM.put("jyestha", "జ్యేష్ఠం / Jyeshtha");
        MASAM.put("ज्येष्ठ", "జ్యేష్ఠం / Jyeshtha");

        MASAM.put("ashadha", "ఆషాఢం / Ashadha");
        MASAM.put("ashada", "ఆషాఢం / Ashadha");
        MASAM.put("आषाढ़", "ఆషాఢం / Ashadha");

        MASAM.put("shravana", "శ్రావణం / Shravana");
        MASAM.put("sravana", "శ్రావణం / Shravana");
        MASAM.put("श्रावण", "శ్రావణం / Shravana");

        MASAM.put("bhadrapada", "భాద్రపదం / Bhadrapada");
        MASAM.put("भाद्रपद", "భాద్రపదం / Bhadrapada");

        MASAM.put("ashwin", "ఆశ్వయుజం / Ashwayuja");
        MASAM.put("ashwayuja", "ఆశ్వయుజం / Ashwayuja");
        MASAM.put("aswayuja", "ఆశ్వయుజం / Ashwayuja");
        MASAM.put("आश्विन", "ఆశ్వయుజం / Ashwayuja");

        MASAM.put("kartika", "కార్తీకం / Kartika");
        MASAM.put("karthika", "కార్తీకం / Kartika");
        MASAM.put("कार्तिक", "కార్తీకం / Kartika");

        MASAM.put("margashirsha", "మార్గశిరం / Margashirsha");
        MASAM.put("margasira", "మార్గశిరం / Margashirsha");
        MASAM.put("मार्गशीर्ष", "మార్గశిరం / Margashirsha");

        MASAM.put("pausha", "పుష్యం / Pausha");
        MASAM.put("pushya", "పుష్యం / Pausha");
        MASAM.put("पौष", "పుష్యం / Pausha");

        MASAM.put("magha", "మాఘం / Magha");
        MASAM.put("माघ", "మాఘం / Magha");

        MASAM.put("phalguna", "ఫాల్గుణం / Phalguna");
        MASAM.put("phalgun", "ఫాల్గుణం / Phalguna");
        MASAM.put("फाल्गुन", "ఫాల్గుణం / Phalguna");
    }

    private static void loadSamvatsaram() {
        SAMVATSARAM.put("prabhava", "ప్రభవ / Prabhava");
        SAMVATSARAM.put("vibhava", "విభవ / Vibhava");
        SAMVATSARAM.put("shukla", "శుక్ల / Shukla");
        SAMVATSARAM.put("sukla", "శుక్ల / Shukla");
        SAMVATSARAM.put("pramodoota", "ప్రమోదూత / Pramodoota");
        SAMVATSARAM.put("pramoda", "ప్రమోదూత / Pramodoota");
        SAMVATSARAM.put("prajotpatti", "ప్రజోత్పత్తి / Prajotpatti");
        SAMVATSARAM.put("angirasa", "ఆంగీరస / Angirasa");
        SAMVATSARAM.put("srimukha", "శ్రీముఖ / Srimukha");
        SAMVATSARAM.put("shrimukha", "శ్రీముఖ / Srimukha");
        SAMVATSARAM.put("bhava", "భావ / Bhava");
        SAMVATSARAM.put("yuva", "యువ / Yuva");
        SAMVATSARAM.put("dhata", "ధాత / Dhata");
        SAMVATSARAM.put("ishvara", "ఈశ్వర / Ishvara");
        SAMVATSARAM.put("eeshwara", "ఈశ్వర / Ishvara");
        SAMVATSARAM.put("bahudhanya", "బహుధాన్య / Bahudhanya");
        SAMVATSARAM.put("pramathi", "ప్రమాథి / Pramathi");
        SAMVATSARAM.put("vikrama", "విక్రమ / Vikrama");
        SAMVATSARAM.put("vrisha", "వృష / Vrisha");
        SAMVATSARAM.put("chitrabhanu", "చిత్రభాను / Chitrabhanu");
        SAMVATSARAM.put("svabhanu", "స్వభాను / Svabhanu");
        SAMVATSARAM.put("swabhanu", "స్వభాను / Svabhanu");
        SAMVATSARAM.put("tarana", "తారణ / Tarana");
        SAMVATSARAM.put("parthiva", "పార్థివ / Parthiva");
        SAMVATSARAM.put("vyaya", "వ్యయ / Vyaya");
        SAMVATSARAM.put("sarvajit", "సర్వజిత్ / Sarvajit");
        SAMVATSARAM.put("sarvadhari", "సర్వధారి / Sarvadhari");
        SAMVATSARAM.put("virodhi", "విరోధి / Virodhi");
        SAMVATSARAM.put("vikriti", "వికృతి / Vikriti");
        SAMVATSARAM.put("khara", "ఖర / Khara");
        SAMVATSARAM.put("nandana", "నందన / Nandana");
        SAMVATSARAM.put("vijaya", "విజయ / Vijaya");
        SAMVATSARAM.put("jaya", "జయ / Jaya");
        SAMVATSARAM.put("manmatha", "మన్మథ / Manmatha");
        SAMVATSARAM.put("durmukhi", "దుర్ముఖి / Durmukhi");
        SAMVATSARAM.put("hevilambi", "హేవిళంబి / Hevilambi");
        SAMVATSARAM.put("hemalambi", "హేవిళంబి / Hevilambi");
        SAMVATSARAM.put("vilambi", "విళంబి / Vilambi");
        SAMVATSARAM.put("vikari", "వికారి / Vikari");
        SAMVATSARAM.put("sharvari", "శార్వరి / Sharvari");
        SAMVATSARAM.put("sarvari", "శార్వరి / Sharvari");
        SAMVATSARAM.put("plava", "ప్లవ / Plava");
        SAMVATSARAM.put("subhakritu", "శుభకృతు / Subhakritu");
        SAMVATSARAM.put("shubhakruthu", "శుభకృతు / Subhakritu");
        SAMVATSARAM.put("shobhakritu", "శోభకృతు / Shobhakritu");
        SAMVATSARAM.put("shobhakruthu", "శోభకృతు / Shobhakritu");
        SAMVATSARAM.put("krodhi", "క్రోధి / Krodhi");
        SAMVATSARAM.put("viswavasu", "విశ్వావసు / Vishvavasu");
        SAMVATSARAM.put("vishvavasu", "విశ్వావసు / Vishvavasu");
        SAMVATSARAM.put("parabhava", "పరాభవ / Parabhava");
        SAMVATSARAM.put("plavanga", "ప్లవంగ / Plavanga");
        SAMVATSARAM.put("kilaka", "కీలక / Kilaka");
        SAMVATSARAM.put("keelaka", "కీలక / Kilaka");
        SAMVATSARAM.put("saumya", "సౌమ్య / Saumya");
        SAMVATSARAM.put("soumya", "సౌమ్య / Saumya");
        SAMVATSARAM.put("sadharana", "సాధారణ / Sadharana");
        SAMVATSARAM.put("virodhikrit", "విరోధికృత్ / Virodhikrit");
        SAMVATSARAM.put("paridhavi", "పరిధావి / Paridhavi");
        SAMVATSARAM.put("pramadi", "ప్రమాది / Pramadi");
        SAMVATSARAM.put("ananda", "ఆనంద / Ananda");
        SAMVATSARAM.put("rakshasa", "రాక్షస / Rakshasa");
        SAMVATSARAM.put("nala", "నల / Nala");
        SAMVATSARAM.put("pingala", "పింగళ / Pingala");
        SAMVATSARAM.put("kalayukthi", "కాలయుక్తి / Kalayukthi");
        SAMVATSARAM.put("kalayukti", "కాలయుక్తి / Kalayukthi");
        SAMVATSARAM.put("siddharthi", "సిద్ధార్థి / Siddharthi");
        SAMVATSARAM.put("raudri", "రౌద్రి / Raudri");
        SAMVATSARAM.put("durmati", "దుర్మతి / Durmati");
        SAMVATSARAM.put("dundubhi", "దుందుభి / Dundubhi");
        SAMVATSARAM.put("rudhirodgari", "రుధిరోద్గారి / Rudhirodgari");
        SAMVATSARAM.put("raktakshi", "రక్తాక్షి / Raktakshi");
        SAMVATSARAM.put("krodhana", "క్రోధన / Krodhana");
        SAMVATSARAM.put("akshaya", "అక్షయ / Akshaya");
    }

    private static void loadAyanam() {
        AYANAM.put("uttarayana", "ఉత్తరాయణం / Uttarayana");
        AYANAM.put("uttaraayana", "ఉత్తరాయణం / Uttarayana");
        AYANAM.put("uttarayanam", "ఉత్తరాయణం / Uttarayana");
        AYANAM.put("uttara ayana", "ఉత్తరాయణం / Uttarayana");
        AYANAM.put("uttarayan", "ఉత్తరాయణం / Uttarayana");
        AYANAM.put("north", "ఉత్తరాయణం / Uttarayana");
        AYANAM.put("northern", "ఉత్తరాయణం / Uttarayana");

        AYANAM.put("dakshinayana", "దక్షిణాయనం / Dakshinayana");
        AYANAM.put("dakshinaayana", "దక్షిణాయనం / Dakshinayana");
        AYANAM.put("dakshinayanam", "దక్షిణాయనం / Dakshinayana");
        AYANAM.put("dakshina ayana", "దక్షిణాయనం / Dakshinayana");
        AYANAM.put("dakshinayan", "దక్షిణాయనం / Dakshinayana");
        AYANAM.put("south", "దక్షిణాయనం / Dakshinayana");
        AYANAM.put("southern", "దక్షిణాయనం / Dakshinayana");
    }

    private static void loadRitu() {
        RITU.put("vasanta", "వసంత ఋతువు / Vasanta Ritu");
        RITU.put("vasantha", "వసంత ఋతువు / Vasanta Ritu");
        RITU.put("spring", "వసంత ఋతువు / Vasanta Ritu");

        RITU.put("grishma", "గ్రీష్మ ఋతువు / Grishma Ritu");
        RITU.put("greeshma", "గ్రీష్మ ఋతువు / Grishma Ritu");
        RITU.put("summer", "గ్రీష్మ ఋతువు / Grishma Ritu");

        RITU.put("varsha", "వర్ష ఋతువు / Varsha Ritu");
        RITU.put("varsha ritu", "వర్ష ఋతువు / Varsha Ritu");
        RITU.put("rainy", "వర్ష ఋతువు / Varsha Ritu");
        RITU.put("monsoon", "వర్ష ఋతువు / Varsha Ritu");

        RITU.put("sharad", "శరదృతువు / Sharad Ritu");
        RITU.put("sharat", "శరదృతువు / Sharad Ritu");
        RITU.put("autumn", "శరదృతువు / Sharad Ritu");

        RITU.put("hemanta", "హేమంత ఋతువు / Hemanta Ritu");
        RITU.put("hemantha", "హేమంత ఋతువు / Hemanta Ritu");
        RITU.put("pre winter", "హేమంత ఋతువు / Hemanta Ritu");
        RITU.put("pre-winter", "హేమంత ఋతువు / Hemanta Ritu");

        RITU.put("shishira", "శిశిర ఋతువు / Shishira Ritu");
        RITU.put("sisira", "శిశిర ఋతువు / Shishira Ritu");
        RITU.put("winter", "శిశిర ఋతువు / Shishira Ritu");
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