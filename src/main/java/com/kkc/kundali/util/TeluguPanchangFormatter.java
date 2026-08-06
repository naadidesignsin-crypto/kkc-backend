package com.kkc.kundali.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TeluguPanchangFormatter {

    private static final Map<String, String> TITHI = new LinkedHashMap<>();
    private static final Map<String, String> NAKSHATRA = new LinkedHashMap<>();
    private static final Map<String, String> YOGA = new LinkedHashMap<>();
    private static final Map<String, String> KARANA = new LinkedHashMap<>();
    private static final Map<String, String> MASAM = new LinkedHashMap<>();
    private static final Map<String, String> PAKSHAM = new LinkedHashMap<>();

    static {
        PAKSHAM.put("shukla", "శుక్ల పక్షం / Shukla Paksha");
        PAKSHAM.put("shukla paksha", "శుక్ల పక్షం / Shukla Paksha");
        PAKSHAM.put("शुक्ल", "శుక్ల పక్షం / Shukla Paksha");
        PAKSHAM.put("शुक्ल पक्ष", "శుక్ల పక్షం / Shukla Paksha");

        PAKSHAM.put("krishna", "కృష్ణ పక్షం / Krishna Paksha");
        PAKSHAM.put("krishna paksha", "కృష్ణ పక్షం / Krishna Paksha");
        PAKSHAM.put("कृष्ण", "కృష్ణ పక్షం / Krishna Paksha");
        PAKSHAM.put("कृष्ण पक्ष", "కృష్ణ పక్షం / Krishna Paksha");

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

        NAKSHATRA.put("ashwini", "అశ్విని / Ashwini");
        NAKSHATRA.put("अश्विनी", "అశ్విని / Ashwini");

        NAKSHATRA.put("bharani", "భరణి / Bharani");
        NAKSHATRA.put("भरणी", "భరణి / Bharani");

        NAKSHATRA.put("kritika", "కృత్తిక / Krittika");
        NAKSHATRA.put("krittika", "కృత్తిక / Krittika");
        NAKSHATRA.put("कृत्तिका", "కృత్తిక / Krittika");

        NAKSHATRA.put("rohini", "రోహిణి / Rohini");
        NAKSHATRA.put("रोहिणी", "రోహిణి / Rohini");

        NAKSHATRA.put("mrigashira", "మృగశిర / Mrigashira");
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

        YOGA.put("vishkambha", "విష్కంభ / Vishkambha");
        YOGA.put("विष्कम्भ", "విష్కంభ / Vishkambha");
        YOGA.put("priti", "ప్రీతి / Priti");
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
        YOGA.put("वैधृति", "వైధృతి / Vaidhriti");

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
        KARANA.put("विष्टि", "విష్టి / Vishti");
        KARANA.put("shakuni", "శకుని / Shakuni");
        KARANA.put("शकुनि", "శకుని / Shakuni");
        KARANA.put("chatushpada", "చతుష్పాద / Chatushpada");
        KARANA.put("चतुष्पद", "చతుష్పాద / Chatushpada");
        KARANA.put("naga", "నాగ / Naga");
        KARANA.put("नाग", "నాగ / Naga");
        KARANA.put("kimstughna", "కింస్తుఘ్న / Kimstughna");
        KARANA.put("किंस्तुघ्न", "కింస్తుఘ్న / Kimstughna");

        MASAM.put("chaitra", "చైత్రం / Chaitra");
        MASAM.put("चैत्र", "చైత్రం / Chaitra");
        MASAM.put("vaishakha", "వైశాఖం / Vaishakha");
        MASAM.put("वैशाख", "వైశాఖం / Vaishakha");
        MASAM.put("jyeshtha", "జ్యేష్ఠం / Jyeshtha");
        MASAM.put("ज्येष्ठ", "జ్యేష్ఠం / Jyeshtha");
        MASAM.put("ashadha", "ఆషాఢం / Ashadha");
        MASAM.put("आषाढ़", "ఆషాఢం / Ashadha");
        MASAM.put("shravana", "శ్రావణం / Shravana");
        MASAM.put("श्रावण", "శ్రావణం / Shravana");
        MASAM.put("bhadrapada", "భాద్రపదం / Bhadrapada");
        MASAM.put("भाद्रपद", "భాద్రపదం / Bhadrapada");
        MASAM.put("ashwin", "ఆశ్వయుజం / Ashwayuja");
        MASAM.put("ashwayuja", "ఆశ్వయుజం / Ashwayuja");
        MASAM.put("आश्विन", "ఆశ్వయుజం / Ashwayuja");
        MASAM.put("kartika", "కార్తీకం / Kartika");
        MASAM.put("कार्तिक", "కార్తీకం / Kartika");
        MASAM.put("margashirsha", "మార్గశిరం / Margashirsha");
        MASAM.put("मार्गशीर्ष", "మార్గశిరం / Margashirsha");
        MASAM.put("pausha", "పుష్యం / Pausha");
        MASAM.put("पौष", "పుష్యం / Pausha");
        MASAM.put("magha", "మాఘం / Magha");
        MASAM.put("माघ", "మాఘం / Magha");
        MASAM.put("phalguna", "ఫాల్గుణం / Phalguna");
        MASAM.put("फाल्गुन", "ఫాల్గుణం / Phalguna");
    }

    private TeluguPanchangFormatter() {
    }

    public static String formatTithi(String raw) {
        return formatComposite(raw, TITHI, true);
    }

    public static String formatNakshatram(String raw) {
        return formatSingle(raw, NAKSHATRA);
    }

    public static String formatYogam(String raw) {
        return formatSingle(raw, YOGA);
    }

    public static String formatKaranam(String raw) {
        return formatSingle(raw, KARANA);
    }

    public static String formatMasam(String raw) {
        return formatSingle(raw, MASAM);
    }

    public static String formatPaksham(String raw) {
        return formatSingle(raw, PAKSHAM);
    }

    private static String formatComposite(
            String raw,
            Map<String, String> map,
            boolean detectPaksha
    ) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }

        String clean = raw.trim();
        String lower = clean.toLowerCase();

        String paksha = "";
        if (detectPaksha) {
            if (containsAny(lower, "shukla", "शुक्ल")) {
                paksha = "శుక్ల పక్షం / Shukla Paksha";
            } else if (containsAny(lower, "krishna", "कृष्ण")) {
                paksha = "కృష్ణ పక్షం / Krishna Paksha";
            }
        }

        String formatted = findMappedValue(clean, map);

        if (!paksha.isBlank() && !formatted.equals(clean)) {
            return paksha + " - " + formatted;
        }

        return formatted;
    }

    private static String formatSingle(String raw, Map<String, String> map) {
        if (raw == null || raw.isBlank()) {
            return "-";
        }

        return findMappedValue(raw.trim(), map);
    }

    private static String findMappedValue(String raw, Map<String, String> map) {
        String lower = raw.toLowerCase();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey().toLowerCase();

            if (lower.equals(key) || lower.contains(key)) {
                return entry.getValue();
            }
        }

        return raw;
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}