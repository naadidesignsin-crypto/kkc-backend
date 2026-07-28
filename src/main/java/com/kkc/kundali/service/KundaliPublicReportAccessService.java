package com.kkc.kundali.service;

import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.repository.KundaliReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class KundaliPublicReportAccessService {

    private final KundaliReportRepository reportRepository;

    public KundaliPublicReportAccessService(KundaliReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public KundaliReport getReport(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kundali report not found"
                ));
    }

    public void assertPlanetsAllowed(Long reportId) {
        KundaliReport report = getReport(reportId);

        if (!Boolean.TRUE.equals(report.getShowPlanets())
                && !Boolean.TRUE.equals(report.getShowBirthChart())
                && !Boolean.TRUE.equals(report.getShowHouses())
                && !Boolean.TRUE.equals(report.getShowNavamsa())
                && !Boolean.TRUE.equals(report.getShowParashara())) {
            deny("Planetary positions are not approved for this Order ID.");
        }
    }

    public void assertDashaAllowed(Long reportId) {
        KundaliReport report = getReport(reportId);

        if (!Boolean.TRUE.equals(report.getShowDasha())
                && !Boolean.TRUE.equals(report.getShowParashara())) {
            deny("Dasha is not approved for this Order ID.");
        }
    }

    public void assertDoshaAllowed(Long reportId) {
        KundaliReport report = getReport(reportId);

        if (!Boolean.TRUE.equals(report.getShowDosha())) {
            deny("Dosha is not approved for this Order ID.");
        }
    }

    public void assertHousesAllowed(Long reportId) {
        KundaliReport report = getReport(reportId);

        if (!Boolean.TRUE.equals(report.getShowHouses())) {
            deny("House analysis is not approved for this Order ID.");
        }
    }

    public void assertNavamsaAllowed(Long reportId) {
        KundaliReport report = getReport(reportId);

        if (!Boolean.TRUE.equals(report.getShowNavamsa())) {
            deny("Navamsa is not approved for this Order ID.");
        }
    }

    public void assertParasharaAllowed(Long reportId) {
        KundaliReport report = getReport(reportId);

        if (!Boolean.TRUE.equals(report.getShowParashara())) {
            deny("Parashara interpretation is not approved for this Order ID.");
        }
    }

    public void assertPdfAllowed(Long reportId) {
        KundaliReport report = getReport(reportId);

        if (!Boolean.TRUE.equals(report.getShowPdf())) {
            deny("PDF download is not approved for this Order ID.");
        }
    }

    private void deny(String message) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
