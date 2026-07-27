package com.kkc.kundali.service;

import com.kkc.kundali.dto.DashaPeriodResponse;
import com.kkc.kundali.dto.KundaliDashaResponse;
import com.kkc.kundali.dto.KundaliGenerateRequest;
import com.kkc.kundali.dto.KundaliReportSectionResponse;
import com.kkc.kundali.dto.ProviderResult;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.entity.KundaliReportSection;
import com.kkc.kundali.mapper.KundaliSectionMapper;
import com.kkc.kundali.repository.KundaliReportRepository;
import com.kkc.kundali.repository.KundaliReportSectionRepository;
import com.kkc.kundali.util.KundaliReportSectionType;
import com.kkc.kundali.util.KundaliReportStatus;
import com.kkc.kundali.util.KundliProviderClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class KundaliReportSectionService {

    private final KundaliReportRepository reportRepository;
    private final KundaliReportSectionRepository sectionRepository;
    private final KundliProviderClient providerClient;
    private final KundaliSectionMapper sectionMapper;

    public KundaliReportSectionService(
            KundaliReportRepository reportRepository,
            KundaliReportSectionRepository sectionRepository,
            KundliProviderClient providerClient,
            KundaliSectionMapper sectionMapper
    ) {
        this.reportRepository = reportRepository;
        this.sectionRepository = sectionRepository;
        this.providerClient = providerClient;
        this.sectionMapper = sectionMapper;
    }

    @Transactional
    public KundaliReportSectionResponse generateSection(
            Long reportId,
            KundaliReportSectionType sectionType
    ) {
        if (!sectionType.isSupportedNow()) {
            throw new IllegalArgumentException(
                    "Section is not configured yet: " + sectionType
            );
        }

        KundaliReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found"));

        Optional<KundaliReportSection> existingSectionOptional =
                sectionRepository.findByReportIdAndSectionType(reportId, sectionType);

        if (existingSectionOptional.isPresent()) {
            KundaliReportSection existingSection = existingSectionOptional.get();

            if (isAlreadyGenerated(existingSection)) {
                syncReportSnapshotFromSection(report, existingSection);
                return KundaliReportSectionResponse.from(existingSection);
            }
        }

        KundaliReportSection section = existingSectionOptional
                .orElseGet(() -> KundaliReportSection.builder()
                        .reportId(reportId)
                        .sectionType(sectionType)
                        .provider("KUNDLI_API")
                        .providerEndpoint(sectionType.getEndpointPath())
                        .build()
                );

        section.setStatus(KundaliReportStatus.PENDING);
        section.setProvider("KUNDLI_API");
        section.setProviderEndpoint(sectionType.getEndpointPath());
        section.setErrorMessage(null);

        section = sectionRepository.save(section);

        try {
            KundaliGenerateRequest request = buildRequestFromReport(report);

            ProviderResult providerResult = providerClient.callEndpoint(
                    sectionType.getEndpointPath(),
                    request
            );

            section.setRequestJson(providerResult.getRequestJson());
            section.setResponseJson(providerResult.getResponseJson());
            section.setStatus(KundaliReportStatus.SUCCESS);
            section.setErrorMessage(null);

            KundaliReportSection savedSection = sectionRepository.save(section);
            syncReportSnapshotFromSection(report, savedSection);

            return KundaliReportSectionResponse.from(savedSection);
        } catch (Exception ex) {
            section.setStatus(KundaliReportStatus.FAILED);
            section.setErrorMessage(ex.getMessage());

            return KundaliReportSectionResponse.from(sectionRepository.save(section));
        }
    }

    @Transactional(readOnly = true)
    public List<KundaliReportSectionResponse> findSections(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new IllegalArgumentException("Kundali report not found");
        }

        return sectionRepository.findByReportIdOrderByCreatedAtAsc(reportId)
                .stream()
                .map(KundaliReportSectionResponse::from)
                .toList();
    }

    private void syncReportSnapshotFromSection(
            KundaliReport report,
            KundaliReportSection section
    ) {
        if (report == null
                || section == null
                || section.getStatus() != KundaliReportStatus.SUCCESS
                || section.getResponseJson() == null
                || section.getResponseJson().isBlank()) {
            return;
        }

        if (section.getSectionType() == KundaliReportSectionType.DASHA) {
            try {
                KundaliDashaResponse dashaResponse = sectionMapper.toDashaResponse(section);
                DashaPeriodResponse currentDasha = dashaResponse.getCurrentDasha();

                if (currentDasha != null && currentDasha.getPlanet() != null) {
                    report.setCurrentDasha(currentDasha.getPlanet());
                    reportRepository.save(report);
                }
            } catch (Exception ignored) {
                // Do not fail section generation just because snapshot update failed.
            }
        }
    }

    private boolean isAlreadyGenerated(KundaliReportSection section) {
        return section.getStatus() == KundaliReportStatus.SUCCESS
                && section.getResponseJson() != null
                && !section.getResponseJson().isBlank();
    }

    private KundaliGenerateRequest buildRequestFromReport(KundaliReport report) {
        return KundaliGenerateRequest.builder()
                .fullName(report.getFullName())
                .gender(report.getGender())
                .dateOfBirth(report.getDateOfBirth())
                .timeOfBirth(report.getTimeOfBirth())
                .birthPlace(report.getBirthPlace())
                .latitude(report.getLatitude())
                .longitude(report.getLongitude())
                .timezone(report.getTimezone())
                .language(report.getLanguage())
                .build();
    }
}