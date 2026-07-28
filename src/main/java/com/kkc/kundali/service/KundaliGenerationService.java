package com.kkc.kundali.service;

import com.kkc.kundali.dto.KundaliGenerateRequest;
import com.kkc.kundali.dto.KundaliReportListItemResponse;
import com.kkc.kundali.dto.KundaliReportPageResponse;
import com.kkc.kundali.dto.KundaliReportResponse;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.dto.ProviderResult;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.mapper.KundaliSummaryMapper;
import com.kkc.kundali.repository.KundaliReportRepository;
import com.kkc.kundali.util.KundaliReportStatus;
import com.kkc.kundali.util.KundliProviderClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public KundaliReportResponse generate(KundaliGenerateRequest request) {
        String orderId = normalizeOrderId(request.getOrderId());

        KundaliReport existingReport = repository.findByOrderId(orderId).orElse(null);

        if (existingReport != null) {
            syncSummarySnapshot(existingReport);
            return KundaliReportResponse.from(repository.save(existingReport));
        }

        KundaliReport report = KundaliReport.builder()
                .orderId(orderId)
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
                .showSummary(true)
                .showConsultation(true)
                .showBirthChart(false)
                .showPlanets(false)
                .showHouses(false)
                .showNavamsa(false)
                .showParashara(false)
                .showDasha(false)
                .showDosha(false)
                .showPdf(false)
                .build();

        report = repository.save(report);

        try {
            ProviderResult providerResult = providerClient.generate(request);

            report.setProviderRequestJson(providerResult.getRequestJson());
            report.setProviderResponseJson(providerResult.getResponseJson());
            report.setStatus(KundaliReportStatus.SUCCESS);
            report.setErrorMessage(null);

            syncSummarySnapshot(report);

            return KundaliReportResponse.from(repository.save(report));
        } catch (Exception ex) {
            report.setStatus(KundaliReportStatus.FAILED);
            report.setErrorMessage(ex.getMessage());

            return KundaliReportResponse.from(repository.save(report));
        }
    }

    @Transactional(readOnly = true)
    public KundaliReportResponse findById(Long id) {
        KundaliReport report = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found"));

        return KundaliReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<KundaliReportResponse> findAll(KundaliReportStatus status) {
        List<KundaliReport> reports = status == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status);

        return reports.stream()
                .map(KundaliReportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public KundaliReportPageResponse findGeneratedReports(
            KundaliReportStatus status,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<KundaliReport> reportPage = status == null
                ? repository.findAllByOrderByCreatedAtDesc(pageable)
                : repository.findByStatusOrderByCreatedAtDesc(status, pageable);

        return new KundaliReportPageResponse(
                reportPage.getContent()
                        .stream()
                        .map(this::toListItem)
                        .toList(),
                reportPage.getNumber(),
                reportPage.getSize(),
                reportPage.getTotalElements(),
                reportPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public KundaliSummaryResponse findSummaryById(Long id) {
        KundaliReport report = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found"));

        return summaryMapper.from(report);
    }

    private KundaliReportListItemResponse toListItem(KundaliReport report) {
        return new KundaliReportListItemResponse(
                report.getId(),
                report.getFullName(),
                report.getGender(),
                report.getDateOfBirth(),
                report.getTimeOfBirth(),
                report.getBirthPlace(),
                report.getProvider(),
                report.getStatus() != null ? report.getStatus().name() : null,
                report.getAscendant(),
                report.getRashi(),
                report.getSignLord(),
                report.getNakshatra(),
                report.getNakshatraLord(),
                report.getCurrentDasha(),
                report.getCreatedAt()
        );
    }

    private void syncSummarySnapshot(KundaliReport report) {
        if (report == null
                || report.getProviderResponseJson() == null
                || report.getProviderResponseJson().isBlank()) {
            return;
        }

        KundaliSummaryResponse summary = summaryMapper.from(report);

        report.setAscendant(summary.getAscendant());
        report.setRashi(summary.getRashi());
        report.setSignLord(summary.getSignLord());
        report.setNakshatra(summary.getNakshatra());
        report.setNakshatraLord(summary.getNakshatraLord());
    }

    private String normalizeOrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }

        return value.trim().toUpperCase();
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