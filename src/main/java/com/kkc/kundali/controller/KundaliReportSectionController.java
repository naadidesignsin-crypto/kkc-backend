package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliReportSectionResponse;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.service.KundaliPublicReportAccessService;
import com.kkc.kundali.service.KundaliReportSectionService;
import com.kkc.kundali.util.KundaliReportSectionType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/kundali/reports/{reportId}/sections")
public class KundaliReportSectionController {

    private final KundaliReportSectionService service;
    private final KundaliPublicReportAccessService accessService;

    public KundaliReportSectionController(
            KundaliReportSectionService service,
            KundaliPublicReportAccessService accessService
    ) {
        this.service = service;
        this.accessService = accessService;
    }

    @GetMapping
    public List<KundaliReportSectionResponse> findSections(
            @PathVariable Long reportId,
            @RequestParam String orderId
    ) {
        KundaliReport report = accessService.requireReportForOrder(reportId, orderId);

        if (!Boolean.TRUE.equals(report.getShowBirthChart())
                && !Boolean.TRUE.equals(report.getShowPlanets())
                && !Boolean.TRUE.equals(report.getShowHouses())
                && !Boolean.TRUE.equals(report.getShowNavamsa())
                && !Boolean.TRUE.equals(report.getShowParashara())
                && !Boolean.TRUE.equals(report.getShowDasha())
                && !Boolean.TRUE.equals(report.getShowDosha())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Advanced sections are not approved for this Order ID.");
        }

        return service.findSections(reportId);
    }

    @PostMapping("/{sectionType}/generate")
    public KundaliReportSectionResponse generateSection(
            @PathVariable Long reportId,
            @PathVariable KundaliReportSectionType sectionType
    ) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Public section generation is disabled. Admin approval generates sections."
        );
    }
}
