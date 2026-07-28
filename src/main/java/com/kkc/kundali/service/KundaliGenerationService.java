package com.kkc.kundali.service;

import com.kkc.kundali.dto.KundaliGenerateRequest;
import com.kkc.kundali.dto.KundaliReportResponse;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.dto.ProviderResult;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.mapper.KundaliSummaryMapper;
import com.kkc.kundali.repository.KundaliReportRepository;
import com.kkc.kundali.util.KundaliOrderIdGenerator;
import com.kkc.kundali.util.KundaliReportStatus;
import com.kkc.kundali.util.KundliProviderClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KundaliGenerationService {

    private final KundaliReportRepository repository;
    private final KundliProviderClient providerClient;
    private final KundaliSummaryMapper summaryMapper;
    private final KundaliOrderIdGenerator orderIdGenerator;

    public KundaliGenerationService(
            KundaliReportRepository repository,
            KundliProviderClient providerClient,
            KundaliSummaryMapper summaryMapper,
            KundaliOrderIdGenerator orderIdGenerator
    ) {
        this.repository = repository;
        this.providerClient = providerClient;
        this.summaryMapper = summaryMapper;
        this.orderIdGenerator = orderIdGenerator;
    }

    @Transactional
    public KundaliReportResponse generate(KundaliGenerateRequest request) {
        KundaliReport report = KundaliReport.builder()
                .orderId(generateUniqueOrderId())
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
        return KundaliReportResponse.from(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public KundaliReportResponse findByOrderId(String orderId) {
        return KundaliReportResponse.from(findEntityByOrderId(orderId));
    }

    @Transactional(readOnly = true)
    public KundaliSummaryResponse findSummaryById(Long id) {
        return summaryMapper.from(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public KundaliSummaryResponse findSummaryByOrderId(String orderId) {
        return summaryMapper.from(findEntityByOrderId(orderId));
    }

    private KundaliReport findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found"));
    }

    private KundaliReport findEntityByOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }

        return repository.findByOrderId(orderId.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found for Order ID: " + orderId));
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

    private String generateUniqueOrderId() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String orderId = orderIdGenerator.generate();
            if (!repository.existsByOrderId(orderId)) {
                return orderId;
            }
        }

        throw new IllegalStateException("Unable to generate unique Order ID");
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
