package com.kkc.kundali.service;

import com.kkc.kundali.dto.*;
import com.kkc.kundali.dto.KundaliGenerateRequest;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.mapper.KundaliSummaryMapper;
import com.kkc.kundali.repository.KundaliReportRepository;
import com.kkc.kundali.util.KundaliReportStatus;
import com.kkc.kundali.util.KundliProviderClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KundaliGenerationService {

    private final KundaliReportRepository repository;
    private final KundliProviderClient providerClient;
    private final KundaliSummaryMapper summaryMapper;

    public KundaliGenerationService(
            KundaliReportRepository repository,
            KundliProviderClient providerClient,
            KundaliSummaryMapper summaryMapper
    ) {
        this.repository = repository;
        this.providerClient = providerClient;
        this.summaryMapper = summaryMapper;
    }

    public KundaliReportResponse generate(KundaliGenerateRequest request) {
        KundaliReport report = KundaliReport.builder()
                .fullName(clean(request.getFullName()))
                .gender(cleanOptional(request.getGender()))
                .dateOfBirth(request.getDateOfBirth())
                .timeOfBirth(request.getTimeOfBirth())
                .birthPlace(clean(request.getBirthPlace()))
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .timezone(clean(request.getTimezone()))
                .language(cleanOptional(request.getLanguage()))
                .provider("KUNDLI_API")
                .status(KundaliReportStatus.PENDING)
                .build();

        report = repository.save(report);

        try {
            ProviderResult providerResult = providerClient.generate(request);

            report.setProviderRequestJson(providerResult.getRequestJson());
            report.setProviderResponseJson(providerResult.getResponseJson());
            report.setStatus(KundaliReportStatus.SUCCESS);
            report.setErrorMessage(null);

            return KundaliReportResponse.from(repository.save(report));

        } catch (Exception ex) {
            report.setStatus(KundaliReportStatus.FAILED);
            report.setErrorMessage(ex.getMessage());

            return KundaliReportResponse.from(repository.save(report));
        }
    }

    public KundaliReportResponse findById(Long id) {
        KundaliReport report = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found"));

        return KundaliReportResponse.from(report);
    }

    public List<KundaliReportResponse> findAll(KundaliReportStatus status) {
        List<KundaliReport> reports = status == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status);

        return reports.stream()
                .map(KundaliReportResponse::from)
                .toList();
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String cleanOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public KundaliSummaryResponse findSummaryById(Long id) {
        KundaliReport report = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found"));

        return summaryMapper.from(report);
    }
}
