package com.kkc.kundali.mapper;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KundaliEnglishNormalizer {

    private static final Map<String, String> VALUE_MAP = Map.ofEntries(
            // Core
            Map.entry("लग्न", "Lagna"),
            Map.entry("पुरुष", "Male"),
            Map.entry("स्त्री", "Female"),

            // Planets
            Map.entry("सूर्य", "Sun"),
            Map.entry("चन्द्रमा", "Moon"),
            Map.entry("मंगल", "Mars"),
            Map.entry("बुध", "Mercury"),
            Map.entry("बृहस्पति", "Jupiter"),
            Map.entry("शुक्र", "Venus"),
            Map.entry("शनि", "Saturn"),
            Map.entry("राहु", "Rahu"),
            Map.entry("केतु", "Ketu"),
            Map.entry("यूरेनस", "Uranus"),
            Map.entry("नेपच्यून", "Neptune"),
            Map.entry("प्लूटो", "Pluto"),

            // Rashi
            Map.entry("मेष", "Aries"),
            Map.entry("वृषभ", "Taurus"),
            Map.entry("मिथुन", "Gemini"),
            Map.entry("कर्क", "Cancer"),
            Map.entry("सिंह", "Leo"),
            Map.entry("कन्या", "Virgo"),
            Map.entry("तुला", "Libra"),
            Map.entry("वृश्चिक", "Scorpio"),
            Map.entry("धनु", "Sagittarius"),
            Map.entry("मकर", "Capricorn"),
            Map.entry("कुम्भ", "Aquarius"),
            Map.entry("मीन", "Pisces"),

            // Nakshatra
            Map.entry("अश्विनी", "Ashwini"),
            Map.entry("भरणी", "Bharani"),
            Map.entry("कृत्तिका", "Krittika"),
            Map.entry("रोहिणी", "Rohini"),
            Map.entry("मृगशिरा", "Mrigashira"),
            Map.entry("आर्द्रा", "Ardra"),
            Map.entry("पुनर्वसु", "Punarvasu"),
            Map.entry("पुष्य", "Pushya"),
            Map.entry("अश्लेषा", "Ashlesha"),
            Map.entry("मघा", "Magha"),
            Map.entry("पूर्वा फाल्गुनी", "Purva Phalguni"),
            Map.entry("उत्तर फाल्गुनी", "Uttara Phalguni"),
            Map.entry("हस्त", "Hasta"),
            Map.entry("चित्रा", "Chitra"),
            Map.entry("स्वाति", "Swati"),
            Map.entry("विशाखा", "Vishakha"),
            Map.entry("अनुराधा", "Anuradha"),
            Map.entry("ज्येष्ठा", "Jyeshtha"),
            Map.entry("मूल", "Mula"),
            Map.entry("पूर्वाषाढ़ा", "Purva Ashadha"),
            Map.entry("पूर्वाषाढ़ा", "Purva Ashadha"),
            Map.entry("उत्तराषाढ़ा", "Uttara Ashadha"),
            Map.entry("उत्तराषाढ़ा", "Uttara Ashadha"),
            Map.entry("श्रवण", "Shravana"),
            Map.entry("धनिष्ठा", "Dhanishta"),
            Map.entry("शतभिषा", "Shatabhisha"),
            Map.entry("पूर्वा भाद्रपद", "Purva Bhadrapada"),
            Map.entry("उत्तर भाद्रपद", "Uttara Bhadrapada"),
            Map.entry("रेवती", "Revati"),

            // Basic astro values
            Map.entry("ब्राह्मण", "Brahmin"),
            Map.entry("क्षत्रिय", "Kshatriya"),
            Map.entry("वैश्य", "Vaishya"),
            Map.entry("शूद्र", "Shudra"),
            Map.entry("जलचर", "Jalachara"),
            Map.entry("चतुष्पद", "Chatushpada"),
            Map.entry("मानव", "Human"),
            Map.entry("गज", "Gaja"),
            Map.entry("देव", "Deva"),
            Map.entry("मनुष्य", "Manushya"),
            Map.entry("राक्षस", "Rakshasa"),
            Map.entry("अंत्य", "Antya"),
            Map.entry("मध्य", "Madhya"),
            Map.entry("आदि", "Adi"),
            Map.entry("वायु", "Vayu"),
            Map.entry("अग्नि", "Agni"),
            Map.entry("पृथ्वी", "Prithvi"),
            Map.entry("जल", "Jala"),
            Map.entry("आकाश", "Akasha"),

            // Tithi / Yoga / Karan / Masa
            Map.entry("कृष्ण पक्ष पंचमी", "Krishna Paksha Panchami"),
            Map.entry("शुक्ल पक्ष पंचमी", "Shukla Paksha Panchami"),
            Map.entry("शूल", "Shula"),
            Map.entry("तैतिल", "Taitila"),
            Map.entry("भाद्रपद", "Bhadrapada"),

            // Planet states
            Map.entry("बाल", "Bala"),
            Map.entry("कुमार", "Kumara"),
            Map.entry("युवा", "Yuva"),
            Map.entry("वृद्ध", "Vriddha"),
            Map.entry("मृत", "Mrita"),

            // Dosha
            Map.entry("द्विबल", "Dwibala"),
            Map.entry("निम्न", "Low"),
            Map.entry("मध्यम", "Medium"),
            Map.entry("उच्च", "High"),

            // Misc
            Map.entry("दो", "Do"),
            Map.entry("सोना (नक्षत्र के आधार पर)", "Gold, based on Nakshatra")
    );

    public String normalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String trimmed = value.trim();

        if (VALUE_MAP.containsKey(trimmed)) {
            return VALUE_MAP.get(trimmed);
        }

        String translated = trimmed;

        // Phrase-level replacements for longer Hindi sentences.
        translated = translated.replace("मंगल आपकी लग्न कुंडली में", "Mars is placed in your Lagna chart in");
        translated = translated.replace("घर में मौजूद है", "house");
        translated = translated.replace("अत: आप मांगलिक हैं", "therefore Mangal Dosha is present");
        translated = translated.replace("मंगल दोष का आकलन", "Mangal Dosha assessment");
        translated = translated.replace("लग्न कुंडली", "Lagna chart");
        translated = translated.replace("जन्म कुंडली", "birth chart");
        translated = translated.replace("चंद्र कुंडली", "Moon chart");
        translated = translated.replace("मंगल ग्रह", "Mars");

        for (Map.Entry<String, String> entry : VALUE_MAP.entrySet()) {
            translated = translated.replace(entry.getKey(), entry.getValue());
        }

        return translated;
    }
}