package com.kkc.kundali.service;

import com.kkc.kundali.dto.DashaPeriodResponse;
import com.kkc.kundali.dto.HouseInterpretationResponse;
import com.kkc.kundali.dto.KundaliDashaResponse;
import com.kkc.kundali.dto.KundaliDoshaResponse;
import com.kkc.kundali.dto.KundaliHouseResponse;
import com.kkc.kundali.dto.KundaliNavamsaResponse;
import com.kkc.kundali.dto.KundaliParasharaReportResponse;
import com.kkc.kundali.dto.KundaliPlanetsResponse;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.dto.ParasharaSectionResponse;
import com.kkc.kundali.dto.PlanetPositionResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class KundaliParasharaService {

    private final KundaliGenerationService kundaliGenerationService;
    private final KundaliDisplayService kundaliDisplayService;
    private final KundaliHouseService kundaliHouseService;
    private final KundaliNavamsaService kundaliNavamsaService;

    public KundaliParasharaService(
            KundaliGenerationService kundaliGenerationService,
            KundaliDisplayService kundaliDisplayService,
            KundaliHouseService kundaliHouseService,
            KundaliNavamsaService kundaliNavamsaService
    ) {
        this.kundaliGenerationService = kundaliGenerationService;
        this.kundaliDisplayService = kundaliDisplayService;
        this.kundaliHouseService = kundaliHouseService;
        this.kundaliNavamsaService = kundaliNavamsaService;
    }

    public KundaliParasharaReportResponse getParasharaReport(Long reportId) {
        KundaliSummaryResponse summary = kundaliGenerationService.findSummaryById(reportId);
        KundaliPlanetsResponse planets = kundaliDisplayService.getPlanets(reportId);
        KundaliHouseResponse houses = kundaliHouseService.getHouseInterpretations(reportId);
        KundaliNavamsaResponse navamsa = kundaliNavamsaService.getNavamsa(reportId);
        KundaliDashaResponse dasha = kundaliDisplayService.getDasha(reportId);
        KundaliDoshaResponse dosha = kundaliDisplayService.getDosha(reportId);

        List<ParasharaSectionResponse> sections = List.of(
                buildCareerSection(summary, planets, houses, dasha),
                buildMarriageSection(summary, planets, houses, navamsa, dasha, dosha),
                buildFinanceSection(summary, planets, houses, dasha),
                buildHealthSection(summary, planets, houses, dasha),
                buildEducationSection(summary, planets, houses, dasha),
                buildDharmaSection(summary, planets, houses, navamsa),
                buildRemediesSection(summary, planets, dosha, dasha)
        );

        return KundaliParasharaReportResponse.builder()
                .reportId(reportId)
                .sectionType("PARASHARA_INTERPRETATION")
                .status(resolveStatus(summary, planets, houses, navamsa, dasha, dosha))
                .lagna(summary.getAscendant())
                .rashi(summary.getRashi())
                .nakshatra(summary.getNakshatra())
                .currentDasha(currentDashaPlanet(dasha))
                .navamsaAscendant(navamsa == null ? null : navamsa.getNavamsaAscendant())
                .sections(sections)
                .build();
    }

    private ParasharaSectionResponse buildCareerSection(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliHouseResponse houses,
            KundaliDashaResponse dasha
    ) {
        HouseInterpretationResponse tenthHouse = findHouse(houses, 10);
        HouseInterpretationResponse sixthHouse = findHouse(houses, 6);
        HouseInterpretationResponse eleventhHouse = findHouse(houses, 11);

        String planetsInTenth = planetNamesInHouse(planets, 10);
        String currentDasha = currentDashaPlanet(dasha);

        List<String> observations = new ArrayList<>();
        observations.add("The 10th house is the main house for career, profession, public role, authority, and karma.");
        observations.add("10th house focus: " + safeArea(tenthHouse));
        observations.add("Planets placed in the 10th house: " + planetsInTenth + ".");
        observations.add("The 6th house should be checked for service, competition, daily work, debts, and obstacles.");
        observations.add("The 11th house should be checked for income, gains, professional network, and fulfilment of goals.");
        observations.add("Current Dasha influence: " + safe(currentDasha) + ".");

        return ParasharaSectionResponse.builder()
                .sectionKey("CAREER")
                .title("Career and Professional Direction")
                .summary("Career assessment in Parashara astrology is primarily checked through the 10th house, supported by the 6th house, 11th house, planetary strength, and current Dasha.")
                .focusAreas(List.of(
                        "10th house - career and public work",
                        "6th house - service and competition",
                        "11th house - income and gains",
                        "Current Dasha - timing of professional results"
                ))
                .observations(observations)
                .guidance("For career prediction, review the 10th house, planets placed there, the 10th lord, current Dasha, and whether the active planets connect with the 2nd, 6th, 10th, or 11th houses.")
                .caution("This is a structured report interpretation, not a final personalized prediction. Final judgement requires house lord strength, aspects, dignity, divisional charts, and astrologer review.")
                .build();
    }

    private ParasharaSectionResponse buildMarriageSection(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliHouseResponse houses,
            KundaliNavamsaResponse navamsa,
            KundaliDashaResponse dasha,
            KundaliDoshaResponse dosha
    ) {
        HouseInterpretationResponse seventhHouse = findHouse(houses, 7);
        String planetsInSeventh = planetNamesInHouse(planets, 7);
        String navamsaAscendant = navamsa == null ? null : navamsa.getNavamsaAscendant();

        boolean mangalPresent = dosha != null && Boolean.TRUE.equals(dosha.getMangalDoshaPresent());

        List<String> observations = new ArrayList<>();
        observations.add("The 7th house is the primary house for marriage, spouse, agreements, and partnerships.");
        observations.add("7th house focus: " + safeArea(seventhHouse));
        observations.add("Planets placed in the 7th house: " + planetsInSeventh + ".");
        observations.add("Navamsa / D9 is important for marriage and long-term relationship strength.");
        observations.add("Navamsa Ascendant: " + safe(navamsaAscendant) + ".");
        observations.add("Mangal Dosha status: " + (mangalPresent ? "Present" : "Not Present") + ".");
        observations.add("Current Dasha influence: " + safe(currentDashaPlanet(dasha)) + ".");

        return ParasharaSectionResponse.builder()
                .sectionKey("MARRIAGE")
                .title("Marriage and Relationship Indications")
                .summary("Marriage assessment is checked through the 7th house, Venus/Jupiter influence, Navamsa chart, Mangal Dosha, and Dasha timing.")
                .focusAreas(List.of(
                        "7th house - marriage and spouse",
                        "Navamsa / D9 - relationship strength",
                        "Venus and Jupiter - marriage significators",
                        "Mangal Dosha - compatibility consideration",
                        "Dasha - timing"
                ))
                .observations(observations)
                .guidance("Marriage analysis should combine the 7th house, 7th lord, Venus or Jupiter condition, Navamsa chart, Mangal Dosha, and current Dasha. A single factor should not be used alone.")
                .caution("Do not finalize marriage compatibility only from this section. Detailed matching requires both charts, Guna matching, Dosha checks, Dasha compatibility, and astrologer validation.")
                .build();
    }

    private ParasharaSectionResponse buildFinanceSection(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliHouseResponse houses,
            KundaliDashaResponse dasha
    ) {
        HouseInterpretationResponse secondHouse = findHouse(houses, 2);
        HouseInterpretationResponse eleventhHouse = findHouse(houses, 11);
        HouseInterpretationResponse twelfthHouse = findHouse(houses, 12);

        List<String> observations = new ArrayList<>();
        observations.add("The 2nd house shows accumulated wealth, family resources, speech, savings, and value system.");
        observations.add("2nd house focus: " + safeArea(secondHouse));
        observations.add("Planets placed in the 2nd house: " + planetNamesInHouse(planets, 2) + ".");
        observations.add("The 11th house shows income, gains, network, and fulfilment of desires.");
        observations.add("Planets placed in the 11th house: " + planetNamesInHouse(planets, 11) + ".");
        observations.add("The 12th house shows expenses, losses, foreign spending, isolation, and spiritual expenditure.");
        observations.add("Planets placed in the 12th house: " + planetNamesInHouse(planets, 12) + ".");
        observations.add("Current Dasha influence: " + safe(currentDashaPlanet(dasha)) + ".");

        return ParasharaSectionResponse.builder()
                .sectionKey("FINANCE")
                .title("Finance, Wealth, and Gains")
                .summary("Finance is assessed through the 2nd house for savings, 11th house for gains, 12th house for expenses, and Dasha for timing.")
                .focusAreas(List.of(
                        "2nd house - wealth and savings",
                        "11th house - gains and income",
                        "12th house - expenses and losses",
                        "Dasha - timing of financial results"
                ))
                .observations(observations)
                .guidance("Financial interpretation should compare wealth houses with expense houses. Strong connections between 2nd, 10th, and 11th houses usually support earnings, while strong 12th house influence requires expense control.")
                .caution("This section should not be treated as financial advice. It is an astrology-based interpretation and should be reviewed with practical financial planning.")
                .build();
    }

    private ParasharaSectionResponse buildHealthSection(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliHouseResponse houses,
            KundaliDashaResponse dasha
    ) {
        HouseInterpretationResponse firstHouse = findHouse(houses, 1);
        HouseInterpretationResponse sixthHouse = findHouse(houses, 6);
        HouseInterpretationResponse eighthHouse = findHouse(houses, 8);
        HouseInterpretationResponse twelfthHouse = findHouse(houses, 12);

        List<String> observations = new ArrayList<>();
        observations.add("The 1st house shows body, vitality, appearance, confidence, and general health.");
        observations.add("1st house focus: " + safeArea(firstHouse));
        observations.add("Planets placed in the 1st house: " + planetNamesInHouse(planets, 1) + ".");
        observations.add("The 6th house shows illness, debts, enemies, competition, and ability to fight obstacles.");
        observations.add("Planets placed in the 6th house: " + planetNamesInHouse(planets, 6) + ".");
        observations.add("The 8th house shows chronic issues, sudden events, transformation, and longevity matters.");
        observations.add("Planets placed in the 8th house: " + planetNamesInHouse(planets, 8) + ".");
        observations.add("The 12th house shows sleep, hospitalization, losses, and withdrawal.");
        observations.add("Planets placed in the 12th house: " + planetNamesInHouse(planets, 12) + ".");
        observations.add("Current Dasha influence: " + safe(currentDashaPlanet(dasha)) + ".");

        return ParasharaSectionResponse.builder()
                .sectionKey("HEALTH")
                .title("Health and Vitality")
                .summary("Health is assessed through the 1st house, 6th house, 8th house, 12th house, planetary strength, and Dasha influence.")
                .focusAreas(List.of(
                        "1st house - body and vitality",
                        "6th house - diseases and obstacles",
                        "8th house - chronic and sudden issues",
                        "12th house - sleep, isolation, hospitalization"
                ))
                .observations(observations)
                .guidance("Health analysis should combine Lagna strength, 6th house condition, malefic influence, Dasha, and planetary dignity. Benefic influence can reduce severity.")
                .caution("This is not medical advice. For health concerns, consult a qualified medical professional.")
                .build();
    }

    private ParasharaSectionResponse buildEducationSection(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliHouseResponse houses,
            KundaliDashaResponse dasha
    ) {
        HouseInterpretationResponse fourthHouse = findHouse(houses, 4);
        HouseInterpretationResponse fifthHouse = findHouse(houses, 5);
        HouseInterpretationResponse ninthHouse = findHouse(houses, 9);

        List<String> observations = new ArrayList<>();
        observations.add("The 4th house shows formal education, learning foundation, and inner stability.");
        observations.add("Planets placed in the 4th house: " + planetNamesInHouse(planets, 4) + ".");
        observations.add("The 5th house shows intelligence, memory, creativity, decision-making, mantra, and past-life merit.");
        observations.add("Planets placed in the 5th house: " + planetNamesInHouse(planets, 5) + ".");
        observations.add("The 9th house shows higher education, dharma, teachers, blessings, and higher wisdom.");
        observations.add("Planets placed in the 9th house: " + planetNamesInHouse(planets, 9) + ".");
        observations.add("Current Dasha influence: " + safe(currentDashaPlanet(dasha)) + ".");

        return ParasharaSectionResponse.builder()
                .sectionKey("EDUCATION")
                .title("Education, Intelligence, and Learning")
                .summary("Education is assessed through the 4th house, 5th house, 9th house, Mercury, Jupiter, and Dasha periods.")
                .focusAreas(List.of(
                        "4th house - basic education",
                        "5th house - intelligence and memory",
                        "9th house - higher learning",
                        "Mercury and Jupiter - learning ability and wisdom"
                ))
                .observations(observations)
                .guidance("Education-related interpretation should review the 4th, 5th, and 9th houses along with Mercury, Jupiter, and current Dasha. Strong 5th and 9th house influence supports deeper learning.")
                .caution("Academic results depend on effort, environment, discipline, and practical preparation along with astrological timing.")
                .build();
    }

    private ParasharaSectionResponse buildDharmaSection(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliHouseResponse houses,
            KundaliNavamsaResponse navamsa
    ) {
        HouseInterpretationResponse fifthHouse = findHouse(houses, 5);
        HouseInterpretationResponse ninthHouse = findHouse(houses, 9);
        HouseInterpretationResponse twelfthHouse = findHouse(houses, 12);

        List<String> observations = new ArrayList<>();
        observations.add("The 5th house shows mantra, devotion, creativity, and past-life merit.");
        observations.add("Planets placed in the 5th house: " + planetNamesInHouse(planets, 5) + ".");
        observations.add("The 9th house shows dharma, gurus, father, blessings, higher wisdom, and fortune.");
        observations.add("Planets placed in the 9th house: " + planetNamesInHouse(planets, 9) + ".");
        observations.add("The 12th house shows moksha, isolation, sleep, expenses, foreign lands, and spiritual withdrawal.");
        observations.add("Planets placed in the 12th house: " + planetNamesInHouse(planets, 12) + ".");
        observations.add("Navamsa Ascendant: " + safe(navamsa == null ? null : navamsa.getNavamsaAscendant()) + ".");

        return ParasharaSectionResponse.builder()
                .sectionKey("DHARMA_SPIRITUALITY")
                .title("Dharma, Spirituality, and Inner Growth")
                .summary("Spiritual and dharmic tendencies are assessed through the 5th, 9th, and 12th houses, supported by Navamsa and Jupiter influence.")
                .focusAreas(List.of(
                        "5th house - mantra and past-life merit",
                        "9th house - dharma and blessings",
                        "12th house - moksha and spiritual withdrawal",
                        "Navamsa - deeper dharmic strength"
                ))
                .observations(observations)
                .guidance("Spiritual interpretation should review 5th, 9th, and 12th houses, Jupiter influence, Navamsa strength, and the active Dasha.")
                .caution("Spiritual practices should be guided with maturity and should not replace practical responsibilities.")
                .build();
    }

    private ParasharaSectionResponse buildRemediesSection(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliDoshaResponse dosha,
            KundaliDashaResponse dasha
    ) {
        boolean mangalPresent = dosha != null && Boolean.TRUE.equals(dosha.getMangalDoshaPresent());
        String currentDasha = currentDashaPlanet(dasha);

        List<String> observations = new ArrayList<>();
        observations.add("Current Dasha planet: " + safe(currentDasha) + ".");
        observations.add("Mangal Dosha status: " + (mangalPresent ? "Present" : "Not Present") + ".");
        observations.add("Lagna: " + safe(summary.getAscendant()) + ".");
        observations.add("Rashi: " + safe(summary.getRashi()) + ".");
        observations.add("Nakshatra: " + safe(summary.getNakshatra()) + ".");

        List<String> focusAreas = new ArrayList<>();
        focusAreas.add("Daily discipline");
        focusAreas.add("Prayer and mantra practice");
        focusAreas.add("Charity and seva");
        focusAreas.add("Respect for parents, teachers, and elders");
        focusAreas.add("Practical correction of habits");

        if (mangalPresent) {
            focusAreas.add("Mangal Dosha-related review before marriage decisions");
        }

        return ParasharaSectionResponse.builder()
                .sectionKey("GENERAL_REMEDIES")
                .title("General Remedies and Practical Guidance")
                .summary("Remedies are suggested only in a general and safe manner. Specific gemstone, mantra count, or ritual remedy should be prescribed only after expert review.")
                .focusAreas(focusAreas)
                .observations(observations)
                .guidance(buildGeneralRemedyGuidance(mangalPresent, currentDasha))
                .caution("Do not prescribe gemstones, expensive rituals, or strong remedial measures automatically. These require astrologer validation.")
                .build();
    }

    private String buildGeneralRemedyGuidance(boolean mangalPresent, String currentDasha) {
        StringBuilder guidance = new StringBuilder();

        guidance.append("Maintain regular prayer, disciplined routine, charity, respect for elders, and practical responsibility. ");

        if (currentDasha != null && !currentDasha.isBlank()) {
            guidance.append("Since the current Dasha is ")
                    .append(currentDasha)
                    .append(", its house placement, dignity, and relationship with Lagna should be reviewed before giving specific remedies. ");
        }

        if (mangalPresent) {
            guidance.append("Because Mangal Dosha is indicated, marriage-related decisions should include detailed compatibility review rather than relying on one factor. ");
        }

        guidance.append("Safe general remedies should be preferred until a complete astrologer review is done.");

        return guidance.toString();
    }

    private HouseInterpretationResponse findHouse(KundaliHouseResponse houses, int houseNumber) {
        if (houses == null || houses.getHouses() == null) {
            return null;
        }

        return houses.getHouses()
                .stream()
                .filter(house -> house.getHouseNumber() != null)
                .filter(house -> house.getHouseNumber() == houseNumber)
                .findFirst()
                .orElse(null);
    }

    private String planetNamesInHouse(KundaliPlanetsResponse planets, int houseNumber) {
        if (planets == null || planets.getPlanets() == null) {
            return "No direct planet placement";
        }

        List<String> names = planets.getPlanets()
                .stream()
                .filter(planet -> planet.getHouse() != null)
                .filter(planet -> planet.getHouse() == houseNumber)
                .map(PlanetPositionResponse::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();

        if (names.isEmpty()) {
            return "No direct planet placement";
        }

        return String.join(", ", names);
    }

    private String currentDashaPlanet(KundaliDashaResponse dasha) {
        if (dasha == null || dasha.getCurrentDasha() == null) {
            return null;
        }

        DashaPeriodResponse current = dasha.getCurrentDasha();

        if (current.getPlanet() == null || current.getPlanet().isBlank()) {
            return null;
        }

        return current.getPlanet();
    }

    private String safeArea(HouseInterpretationResponse house) {
        if (house == null || house.getMainArea() == null || house.getMainArea().isBlank()) {
            return "Not available";
        }

        return house.getMainArea();
    }

    private String resolveStatus(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliHouseResponse houses,
            KundaliNavamsaResponse navamsa,
            KundaliDashaResponse dasha,
            KundaliDoshaResponse dosha
    ) {
        if (summary != null && summary.getStatus() != null) {
            return String.valueOf(summary.getStatus());
        }

        if (planets != null && planets.getStatus() != null) {
            return planets.getStatus();
        }

        if (houses != null && houses.getStatus() != null) {
            return houses.getStatus();
        }

        if (navamsa != null && navamsa.getStatus() != null) {
            return navamsa.getStatus();
        }

        if (dasha != null && dasha.getStatus() != null) {
            return dasha.getStatus();
        }

        if (dosha != null && dosha.getStatus() != null) {
            return dosha.getStatus();
        }

        return "UNKNOWN";
    }

    private String safe(Object value) {
        if (value == null) {
            return "Not available";
        }

        String text = String.valueOf(value).trim();

        if (text.isBlank()) {
            return "Not available";
        }

        return text;
    }
}