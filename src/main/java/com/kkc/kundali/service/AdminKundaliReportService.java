package com.kkc.kundali.service;

import com.kkc.kundali.dto.AdminDeleteKundaliReportResponse;
import com.kkc.kundali.dto.AdminKundaliReportListItemResponse;
import com.kkc.kundali.dto.AdminKundaliReportPageResponse;
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

    public AdminKundaliReportService(
            KundaliReportRepository reportRepository,
            KundaliReportSectionRepository sectionRepository
    ) {
        this.reportRepository = reportRepository;
        this.sectionRepository = sectionRepository;
    }

    @Transactional(readOnly = true)
    public AdminKundaliReportPageResponse findReports(
            KundaliReportStatus status,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<KundaliReport> reportPage = status == null
                ? reportRepository.findAllByOrderByCreatedAtDesc(pageable)
                : reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

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
                report.getCreatedAt()
        );
    }
}