package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliReportSectionResponse;
import com.kkc.kundali.service.KundaliReportSectionService;
import com.kkc.kundali.util.KundaliReportSectionType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kundali/reports/{reportId}/sections")
public class KundaliReportSectionController {

    private final KundaliReportSectionService service;

    public KundaliReportSectionController(KundaliReportSectionService service) {
        this.service = service;
    }

    @GetMapping
    public List<KundaliReportSectionResponse> findSections(
            @PathVariable Long reportId
    ) {
        return service.findSections(reportId);
    }

    @PostMapping("/{sectionType}/generate")
    public KundaliReportSectionResponse generateSection(
            @PathVariable Long reportId,
            @PathVariable KundaliReportSectionType sectionType
    ) {
        return service.generateSection(reportId, sectionType);
    }
}