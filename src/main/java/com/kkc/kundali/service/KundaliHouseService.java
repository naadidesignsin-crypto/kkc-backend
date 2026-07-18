package com.kkc.kundali.service;

import com.kkc.kundali.dto.HouseInterpretationResponse;
import com.kkc.kundali.dto.HousePlanetResponse;
import com.kkc.kundali.dto.KundaliHouseResponse;
import com.kkc.kundali.dto.KundaliPlanetsResponse;
import com.kkc.kundali.dto.PlanetPositionResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KundaliHouseService {

    private final KundaliDisplayService kundaliDisplayService;

    public KundaliHouseService(KundaliDisplayService kundaliDisplayService) {
        this.kundaliDisplayService = kundaliDisplayService;
    }

    public KundaliHouseResponse getHouseInterpretations(Long reportId) {
        KundaliPlanetsResponse planetsResponse = kundaliDisplayService.getPlanets(reportId);

        List<HouseInterpretationResponse> houses = new ArrayList<>();

        for (int houseNumber = 1; houseNumber <= 12; houseNumber++) {
            final int currentHouseNumber = houseNumber;

            List<HousePlanetResponse> planetsInHouse = planetsResponse.getPlanets()
                    .stream()
                    .filter(planet -> planet.getHouse() != null)
                    .filter(planet -> planet.getHouse() == currentHouseNumber)
                    .map(this::toHousePlanet)
                    .toList();

            houses.add(
                    HouseInterpretationResponse.builder()
                            .houseNumber(currentHouseNumber)
                            .houseName(getHouseName(currentHouseNumber))
                            .mainArea(getMainArea(currentHouseNumber))
                            .meaning(getMeaning(currentHouseNumber))
                            .interpretation(buildInterpretation(currentHouseNumber, planetsInHouse))
                            .planets(planetsInHouse)
                            .build()
            );
        }

        return KundaliHouseResponse.builder()
                .reportId(reportId)
                .sectionType("HOUSE_INTERPRETATION")
                .status(planetsResponse.getStatus())
                .houses(houses)
                .build();
    }

    private HousePlanetResponse toHousePlanet(PlanetPositionResponse planet) {
        return HousePlanetResponse.builder()
                .name(planet.getName())
                .rashi(planet.getRashi())
                .nakshatra(planet.getNakshatra())
                .degree(planet.getDegree())
                .retrograde(planet.getRetrograde())
                .combust(planet.getCombust())
                .build();
    }

    private String buildInterpretation(
            int houseNumber,
            List<HousePlanetResponse> planets
    ) {
        String baseMeaning = getMeaning(houseNumber);

        if (planets == null || planets.isEmpty()) {
            return baseMeaning
                    + " No planet is directly placed in this house. Interpretation should be done using house lord, aspects, and dasha influence.";
        }

        String planetNames = planets.stream()
                .map(HousePlanetResponse::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList()
                .toString()
                .replace("[", "")
                .replace("]", "");

        return baseMeaning
                + " Planetary placement in this house: "
                + planetNames
                + ". This house becomes active through these planetary influences. Final prediction should also consider house lord, strength, aspects, nakshatra, and running dasha.";
    }

    private String getHouseName(int houseNumber) {
        return switch (houseNumber) {
            case 1 -> "First House";
            case 2 -> "Second House";
            case 3 -> "Third House";
            case 4 -> "Fourth House";
            case 5 -> "Fifth House";
            case 6 -> "Sixth House";
            case 7 -> "Seventh House";
            case 8 -> "Eighth House";
            case 9 -> "Ninth House";
            case 10 -> "Tenth House";
            case 11 -> "Eleventh House";
            case 12 -> "Twelfth House";
            default -> "Unknown House";
        };
    }

    private String getMainArea(int houseNumber) {
        return switch (houseNumber) {
            case 1 -> "Self, personality, body, life direction";
            case 2 -> "Family, speech, wealth, food habits";
            case 3 -> "Courage, communication, siblings, efforts";
            case 4 -> "Mother, home, property, comfort, inner peace";
            case 5 -> "Education, intelligence, children, creativity";
            case 6 -> "Health issues, debts, enemies, service";
            case 7 -> "Marriage, partnerships, public dealing";
            case 8 -> "Longevity, transformation, secrets, sudden events";
            case 9 -> "Fortune, dharma, father, higher wisdom";
            case 10 -> "Career, profession, karma, public status";
            case 11 -> "Income, gains, network, fulfilment of desires";
            case 12 -> "Expenses, foreign lands, sleep, spirituality, isolation";
            default -> "Not available";
        };
    }

    private String getMeaning(int houseNumber) {
        return switch (houseNumber) {
            case 1 -> "The first house shows personality, physical body, confidence, general health, and the overall direction of life.";
            case 2 -> "The second house shows family background, accumulated wealth, speech, food habits, and value system.";
            case 3 -> "The third house shows courage, communication skills, younger siblings, short travel, and self-effort.";
            case 4 -> "The fourth house shows mother, home, property, vehicles, emotional security, and domestic comfort.";
            case 5 -> "The fifth house shows intelligence, education, creativity, children, mantra, past-life merit, and decision-making ability.";
            case 6 -> "The sixth house shows diseases, debts, enemies, competition, daily work, service, and ability to overcome obstacles.";
            case 7 -> "The seventh house shows marriage, spouse, business partnerships, agreements, and public interactions.";
            case 8 -> "The eighth house shows longevity, sudden events, hidden matters, transformation, inheritance, and deep research.";
            case 9 -> "The ninth house shows fortune, dharma, father, teachers, blessings, higher learning, and long-distance travel.";
            case 10 -> "The tenth house shows career, profession, authority, social status, responsibilities, and public actions.";
            case 11 -> "The eleventh house shows income, gains, elder siblings, social network, achievements, and fulfilment of desires.";
            case 12 -> "The twelfth house shows expenses, foreign residence, sleep, isolation, losses, moksha, and spiritual withdrawal.";
            default -> "House meaning is not available.";
        };
    }
}