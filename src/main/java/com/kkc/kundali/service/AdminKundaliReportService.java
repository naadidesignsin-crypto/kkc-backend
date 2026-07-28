package com.kkc.kundali.service;

import com.kkc.kundali.dto.AdminDeleteKundaliReportResponse;
import com.kkc.kundali.dto.AdminKundaliReportApprovalRequest;
import com.kkc.kundali.dto.AdminKundaliReportListItemResponse;
import com.kkc.kundali.dto.AdminKundaliReportPageResponse;
import com.kkc.kundali.dto.KundaliReportResponse;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.repository.KundaliReportRepository;
import com.kkc.kundali.repository.KundaliReportSectionRepository;
import com.kkc.kundali.util.KundaliReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminKundaliReportService {

    private final KundaliReportRepository reportRepository;
    private final KundaliReportSectionRepository sectionRepository;
    private final KundaliReportSectionService sectionService;

    public AdminKundaliReportService(
            KundaliReportRepository reportRepository,
            KundaliReportSectionRepository sectionRepository,
            KundaliReportSectionService sectionService
    ) {
        this.reportRepository = reportRepository;
        this.sectionRepository = sectionRepository;
        this.sectionService = sectionService;
    }

    @Transactional(readOnly = true)
    public AdminKundaliReportPageResponse findReports(
            KundaliReportStatus status,
            String search,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String cleanSearch = search == null ? "" : search.trim();

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<KundaliReport> reportPage = reportRepository.searchReports(status, cleanSearch, pageable);

        return new AdminKundaliReportPageResponse(
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

    @Transactional
    public KundaliReportResponse updateReportAccess(
            Long reportId,
            AdminKundaliReportApprovalRequest request
    ) {
        KundaliReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Kundali report not found: " + reportId));

        report.setShowSummary(booleanValue(request.showSummary(), true));
        report.setShowConsultation(booleanValue(request.showConsultation(), true));
        report.setShowBirthChart(Boolean.TRUE.equals(request.showBirthChart()));
        report.setShowPlanets(Boolean.TRUE.equals(request.showPlanets()));
        report.setShowHouses(Boolean.TRUE.equals(request.showHouses()));
        report.setShowNavamsa(Boolean.TRUE.equals(request.showNavamsa()));
        report.setShowParashara(Boolean.TRUE.equals(request.showParashara()));
        report.setShowDasha(Boolean.TRUE.equals(request.showDasha()));
        report.setShowDosha(Boolean.TRUE.equals(request.showDosha()));
        report.setShowPdf(Boolean.TRUE.equals(request.showPdf()));

        KundaliReport savedReport = reportRepository.save(report);

        sectionService.generateApprovedSections(savedReport);

        return KundaliReportResponse.from(savedReport);
    }

    @Transactional
    public AdminDeleteKundaliReportResponse deleteReport(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new IllegalArgumentException("Kundali report not found: " + reportId);
        }

        long deletedSections = sectionRepository.deleteByReportId(reportId);
        reportRepository.deleteById(reportId);

        return new AdminDeleteKundaliReportResponse(
                true,
                "Kundali report deleted successfully.",
                reportId,
                deletedSections,
                1
        );
    }

    @Transactional
    public AdminDeleteKundaliReportResponse deleteAllReports(String confirm) {
        if (!"DELETE_ALL_KUNDALI_REPORTS".equals(confirm)) {
            throw new IllegalArgumentException(
                    "Invalid confirmation text. Use DELETE_ALL_KUNDALI_REPORTS."
            );
        }

        long sectionCount = sectionRepository.count();
        long reportCount = reportRepository.count();

        sectionRepository.deleteAllInBatch();
        reportRepository.deleteAllInBatch();

        return new AdminDeleteKundaliReportResponse(
                true,
                "All Kundali reports deleted successfully.",
                null,
                sectionCount,
                reportCount
        );
    }

    private AdminKundaliReportListItemResponse toListItem(KundaliReport report) {
        return new AdminKundaliReportListItemResponse(
                report.getId(),
                report.getOrderId(),
                report.getFullName(),
                report.getGender(),
                report.getDateOfBirth(),
                report.getTimeOfBirth(),
                report.getBirthPlace(),
                report.getProvider(),
                report.getStatus() != null ? report.getStatus().name() : null,
                report.getAscendant(),
                report.getRashi(),
                report.getNakshatra(),
                report.getCurrentDasha(),
                report.getShowSummary(),
                report.getShowConsultation(),
                report.getShowBirthChart(),
                report.getShowPlanets(),
                report.getShowHouses(),
                report.getShowNavamsa(),
                report.getShowParashara(),
                report.getShowDasha(),
                report.getShowDosha(),
                report.getShowPdf(),
                report.getCreatedAt()
        );
    }

    private Boolean booleanValue(Boolean value, boolean defaultValue) {
        return value == null ? defaultValue : value;
    }
}
