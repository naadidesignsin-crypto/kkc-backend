package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliParasharaReportResponse;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.service.KundaliParasharaService;
import com.kkc.kundali.service.KundaliPublicReportAccessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports")
public class KundaliParasharaController {

    private final KundaliParasharaService kundaliParasharaService;
    private final KundaliPublicReportAccessService accessService;

    public KundaliParasharaController(
            KundaliParasharaService kundaliParasharaService,
            KundaliPublicReportAccessService accessService
    ) {
        this.kundaliParasharaService = kundaliParasharaService;
        this.accessService = accessService;
    }

    @GetMapping("/{reportId}/parashara")
    public KundaliParasharaReportResponse getParasharaReport(
            @PathVariable Long reportId,
            @RequestParam String orderId
    ) {
        KundaliReport report = accessService.requireReportForOrder(reportId, orderId);
        accessService.requireParasharaAccess(report);

        return kundaliParasharaService.getParasharaReport(reportId);
    }
}
