package com.kkc.kundali.service;

import com.kkc.kundali.dto.*;
import com.kkc.kundali.repository.KundaliReportRepository;
import com.kkc.kundali.repository.KundaliReportSectionRepository;
import com.kkc.kundali.util.KundaliReportSectionType;
import com.kkc.kundali.util.KundaliReportStatus;
import com.kkc.kundali.util.KundliProviderClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KundaliReportSectionService {

    private final KundaliReportRepository reportRepository;
    private final KundaliReportSectionRepository sectionRepository;
    private final KundliProviderClient providerClient;

    public KundaliReportSectionService(
            KundaliReportRepository reportRepository,
            KundaliReportSectionRepository sectionRepository,
            KundliProviderClient providerClient
    ) {
        this.reportRepository = reportRepository;
        this.sectionRepository = sectionRepository;
        this.providerClient = providerClient;
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

        KundaliReportSection section = sectionRepository
                .findByReportIdAndSectionType(reportId, sectionType)
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

            return KundaliReportSectionResponse.from(sectionRepository.save(section));

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