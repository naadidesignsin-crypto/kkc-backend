package com.kkc.kundali.service;

import com.kkc.kundali.dto.KundaliGenerateRequest;
import com.kkc.kundali.dto.KundaliReportResponse;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.dto.ProviderResult;
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
        KundaliReport existingReport = findExistingSuccessfulReport(request);

        if (existingReport != null) {
            return KundaliReportResponse.from(existingReport);
        }

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

    public KundaliSummaryResponse findSummaryById(Long id) {
        KundaliReport report = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found"));

        return summaryMapper.from(report);
    }

    private KundaliReport findExistingSuccessfulReport(KundaliGenerateRequest request) {
        if (request == null
                || request.getDateOfBirth() == null
                || request.getTimeOfBirth() == null) {
            return null;
        }

        return repository
                .findByDateOfBirthAndTimeOfBirthAndStatusOrderByCreatedAtDesc(
                        request.getDateOfBirth(),
                        request.getTimeOfBirth(),
                        KundaliReportStatus.SUCCESS
                )
                .stream()
                .filter(report -> sameText(report.getFullName(), request.getFullName()))
                .filter(report -> sameText(report.getGender(), request.getGender()))
                .filter(report -> sameText(report.getTimezone(), request.getTimezone()))
                .filter(report -> sameNumber(report.getLatitude(), request.getLatitude()))
                .filter(report -> sameNumber(report.getLongitude(), request.getLongitude()))
                .findFirst()
                .orElse(null);
    }

    private boolean sameText(String left, String right) {
        String cleanLeft = cleanOptional(left);
        String cleanRight = cleanOptional(right);

        if (cleanLeft == null || cleanRight == null) {
            return cleanLeft == null && cleanRight == null;
        }

        return cleanLeft.equalsIgnoreCase(cleanRight);
    }

    private boolean sameNumber(Double left, Double right) {
        if (left == null || right == null) {
            return false;
        }

        return Math.abs(left - right) < 0.000001;
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
}