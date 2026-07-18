package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliParasharaReportResponse;
import com.kkc.kundali.service.KundaliParasharaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports")
public class KundaliParasharaController {

    private final KundaliParasharaService kundaliParasharaService;

    public KundaliParasharaController(KundaliParasharaService kundaliParasharaService) {
        this.kundaliParasharaService = kundaliParasharaService;
    }

    @GetMapping("/{reportId}/parashara")
    public KundaliParasharaReportResponse getParasharaReport(@PathVariable Long reportId) {
        return kundaliParasharaService.getParasharaReport(reportId);
    }
}