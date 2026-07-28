package com.kkc.kundali.controller;

import com.kkc.kundali.dto.AdminDeleteKundaliReportResponse;
import com.kkc.kundali.dto.AdminKundaliReportPageResponse;
import com.kkc.kundali.service.AdminKundaliReportService;
import com.kkc.kundali.util.KundaliReportStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/kundali/reports")
public class AdminKundaliReportController {

    private final AdminKundaliReportService adminKundaliReportService;

    public AdminKundaliReportController(
            AdminKundaliReportService adminKundaliReportService
    ) {
        this.adminKundaliReportService = adminKundaliReportService;
    }

    @GetMapping
    public AdminKundaliReportPageResponse findReports(
            @RequestParam(required = false) KundaliReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminKundaliReportService.findReports(status, page, size);
    }

    @DeleteMapping("/{reportId}")
    public AdminDeleteKundaliReportResponse deleteReport(
            @PathVariable Long reportId
    ) {
        return adminKundaliReportService.deleteReport(reportId);
    }

    @DeleteMapping
    public AdminDeleteKundaliReportResponse deleteAllReports(
            @RequestParam String confirm
    ) {
        return adminKundaliReportService.deleteAllReports(confirm);
    }
}