package com.kkc.kundali.service;

import com.kkc.kundali.dto.*;
import com.kkc.kundali.entity.KundaliReportSection;
import com.kkc.kundali.util.KundaliReportSectionType;
import com.kkc.kundali.util.KundaliReportStatus;
import com.kkc.kundali.mapper.KundaliSectionMapper;
import com.kkc.kundali.repository.KundaliReportSectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KundaliDisplayService {

    private final KundaliReportSectionRepository sectionRepository;
    private final KundaliSectionMapper mapper;

    public KundaliDisplayService(
            KundaliReportSectionRepository sectionRepository,
            KundaliSectionMapper mapper
    ) {
        this.sectionRepository = sectionRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public KundaliPlanetsResponse getPlanets(Long reportId) {
        KundaliReportSection section = getSuccessfulSection(
                reportId,
                KundaliReportSectionType.PLANETARY_POSITIONS
        );

        return mapper.toPlanetsResponse(section);
    }

    @Transactional(readOnly = true)
    public KundaliDashaResponse getDasha(Long reportId) {
        KundaliReportSection section = getSuccessfulSection(
                reportId,
                KundaliReportSectionType.DASHA
        );

        return mapper.toDashaResponse(section);
    }

    @Transactional(readOnly = true)
    public KundaliDoshaResponse getDosha(Long reportId) {
        KundaliReportSection section = getSuccessfulSection(
                reportId,
                KundaliReportSectionType.DOSHA
        );

        return mapper.toDoshaResponse(section);
    }

    private KundaliReportSection getSuccessfulSection(
            Long reportId,
            KundaliReportSectionType sectionType
    ) {
        KundaliReportSection section = sectionRepository
                .findByReportIdAndSectionType(reportId, sectionType)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Kundali section not generated yet: " + sectionType
                ));

        if (section.getStatus() != KundaliReportStatus.SUCCESS) {
            throw new IllegalStateException(
                    "Kundali section is not successful yet: " + sectionType
            );
        }

        if (section.getResponseJson() == null || section.getResponseJson().isBlank()) {
            throw new IllegalStateException(
                    "Kundali section response is empty: " + sectionType
            );
        }

        return section;
    }
}