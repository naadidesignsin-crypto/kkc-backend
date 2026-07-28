package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliReportSectionResponse;
import com.kkc.kundali.service.KundaliReportSectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * Admin-only raw section endpoint.
 * Public users must not call /sections/generate or see raw provider section rows.
 */
@RestController
@RequestMapping("/api/admin/kundali/reports/{reportId}/sections")
public class KundaliReportSectionController {

    private final KundaliReportSectionService service;

    public KundaliReportSectionController(KundaliReportSectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<KundaliReportSectionResponse> findSections(@PathVariable Long reportId) {
        return service.findSections(reportId);
    }
}
