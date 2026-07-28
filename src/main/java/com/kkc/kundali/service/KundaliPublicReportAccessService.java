package com.kkc.kundali.service;

import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.repository.KundaliReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KundaliPublicReportAccessService {

    private final KundaliReportRepository reportRepository;

    public KundaliPublicReportAccessService(KundaliReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public KundaliReport requireReportForOrder(Long reportId, String orderId) {
        if (reportId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report ID is required.");
        }

        String cleanOrderId = normalizeOrderId(orderId);

        KundaliReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kundali report not found."));

        if (report.getOrderId() == null || !report.getOrderId().equalsIgnoreCase(cleanOrderId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kundali report not found for this Order ID.");
        }

        return report;
    }

    @Transactional(readOnly = true)
    public KundaliReport requireReportByOrderId(String orderId) {
        String cleanOrderId = normalizeOrderId(orderId);

        return reportRepository.findByOrderId(cleanOrderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kundali report not found for this Order ID."));
    }

    public void requirePlanetsAccess(KundaliReport report) {
        if (!Boolean.TRUE.equals(report.getShowPlanets())
                && !Boolean.TRUE.equals(report.getShowBirthChart())
                && !Boolean.TRUE.equals(report.getShowHouses())
                && !Boolean.TRUE.equals(report.getShowNavamsa())
                && !Boolean.TRUE.equals(report.getShowParashara())) {
            throwForbidden("Planetary positions are not approved for this Order ID.");
        }
    }

    public void requireDashaAccess(KundaliReport report) {
        if (!Boolean.TRUE.equals(report.getShowDasha())
                && !Boolean.TRUE.equals(report.getShowParashara())) {
            throwForbidden("Dasha is not approved for this Order ID.");
        }
    }

    public void requireDoshaAccess(KundaliReport report) {
        if (!Boolean.TRUE.equals(report.getShowDosha())) {
            throwForbidden("Dosha is not approved for this Order ID.");
        }
    }

    public void requireHousesAccess(KundaliReport report) {
        if (!Boolean.TRUE.equals(report.getShowHouses())) {
            throwForbidden("House analysis is not approved for this Order ID.");
        }
    }

    public void requireNavamsaAccess(KundaliReport report) {
        if (!Boolean.TRUE.equals(report.getShowNavamsa())) {
            throwForbidden("Navamsa is not approved for this Order ID.");
        }
    }

    public void requireParasharaAccess(KundaliReport report) {
        if (!Boolean.TRUE.equals(report.getShowParashara())) {
            throwForbidden("Parashara reading is not approved for this Order ID.");
        }
    }

    public void requirePdfAccess(KundaliReport report) {
        if (!Boolean.TRUE.equals(report.getShowPdf())) {
            throwForbidden("PDF report is not approved for this Order ID.");
        }
    }

    public String normalizeOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID is required.");
        }

        return orderId.trim().toUpperCase();
    }

    private void throwForbidden(String message) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
