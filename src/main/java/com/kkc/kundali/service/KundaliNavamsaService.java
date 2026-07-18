package com.kkc.kundali.service;

import com.kkc.kundali.dto.KundaliNavamsaResponse;
import com.kkc.kundali.dto.KundaliPlanetsResponse;
import com.kkc.kundali.dto.NavamsaPlanetResponse;
import com.kkc.kundali.dto.PlanetPositionResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class KundaliNavamsaService {

    private static final double FULL_ZODIAC_DEGREES = 360.0;
    private static final double SIGN_DEGREES = 30.0;
    private static final double NAVAMSA_DEGREES = SIGN_DEGREES / 9.0;

    private static final String[] RASHIS = {
            "Aries",
            "Taurus",
            "Gemini",
            "Cancer",
            "Leo",
            "Virgo",
            "Libra",
            "Scorpio",
            "Sagittarius",
            "Capricorn",
            "Aquarius",
            "Pisces"
    };

    private final KundaliDisplayService kundaliDisplayService;

    public KundaliNavamsaService(KundaliDisplayService kundaliDisplayService) {
        this.kundaliDisplayService = kundaliDisplayService;
    }

    public KundaliNavamsaResponse getNavamsa(Long reportId) {
        KundaliPlanetsResponse planetsResponse = kundaliDisplayService.getPlanets(reportId);

        Integer navamsaAscendantIndex = findNavamsaAscendantIndex(planetsResponse);

        List<NavamsaPlanetResponse> navamsaPlanets = planetsResponse.getPlanets()
                .stream()
                .filter(planet -> planet.getLongitude() != null)
                .map(planet -> toNavamsaPlanet(planet, navamsaAscendantIndex))
                .toList();

        String navamsaAscendant = navamsaAscendantIndex == null
                ? null
                : RASHIS[navamsaAscendantIndex];

        return KundaliNavamsaResponse.builder()
                .reportId(reportId)
                .sectionType("NAVAMSA_CHART")
                .status(planetsResponse.getStatus())
                .navamsaAscendant(navamsaAscendant)
                .planets(navamsaPlanets)
                .build();
    }

    private NavamsaPlanetResponse toNavamsaPlanet(
            PlanetPositionResponse planet,
            Integer navamsaAscendantIndex
    ) {
        double normalizedLongitude = normalizeLongitude(planet.getLongitude());

        int navamsaRashiIndex = calculateNavamsaRashiIndex(normalizedLongitude);
        int navamsaNumber = calculateNavamsaNumber(normalizedLongitude);

        Integer navamsaHouse = navamsaAscendantIndex == null
                ? null
                : calculateHouseFromAscendant(navamsaAscendantIndex, navamsaRashiIndex);

        return NavamsaPlanetResponse.builder()
                .planetName(planet.getName())
                .birthRashi(planet.getRashi())
                .birthHouse(planet.getHouse())
                .birthLongitude(normalizedLongitude)
                .birthNakshatra(planet.getNakshatra())
                .navamsaNumber(navamsaNumber)
                .navamsaRashi(RASHIS[navamsaRashiIndex])
                .navamsaHouse(navamsaHouse)
                .build();
    }

    private Integer findNavamsaAscendantIndex(KundaliPlanetsResponse planetsResponse) {
        if (planetsResponse == null || planetsResponse.getPlanets() == null) {
            return null;
        }

        return planetsResponse.getPlanets()
                .stream()
                .filter(planet -> planet.getName() != null)
                .filter(planet -> isLagna(planet.getName()))
                .filter(planet -> planet.getLongitude() != null)
                .findFirst()
                .map(planet -> calculateNavamsaRashiIndex(normalizeLongitude(planet.getLongitude())))
                .orElse(null);
    }

    private boolean isLagna(String planetName) {
        String cleanName = planetName.trim().toLowerCase(Locale.ENGLISH);

        return cleanName.equals("lagna")
                || cleanName.equals("ascendant")
                || cleanName.equals("लग्न");
    }

    private double normalizeLongitude(Double longitude) {
        if (longitude == null) {
            return 0.0;
        }

        double normalized = longitude % FULL_ZODIAC_DEGREES;

        if (normalized < 0) {
            normalized += FULL_ZODIAC_DEGREES;
        }

        return normalized;
    }

    private int calculateNavamsaRashiIndex(double normalizedLongitude) {
        int index = (int) Math.floor(normalizedLongitude / NAVAMSA_DEGREES);

        return index % 12;
    }

    private int calculateNavamsaNumber(double normalizedLongitude) {
        double signDegree = normalizedLongitude % SIGN_DEGREES;

        return ((int) Math.floor(signDegree / NAVAMSA_DEGREES)) + 1;
    }

    private int calculateHouseFromAscendant(
            int navamsaAscendantIndex,
            int navamsaRashiIndex
    ) {
        return ((navamsaRashiIndex - navamsaAscendantIndex + 12) % 12) + 1;
    }
}