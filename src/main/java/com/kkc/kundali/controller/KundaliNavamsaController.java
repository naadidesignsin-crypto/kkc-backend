package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliNavamsaResponse;
import com.kkc.kundali.service.KundaliNavamsaService;
import com.kkc.kundali.service.KundaliPublicReportAccessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports")
public class KundaliNavamsaController {

    private final KundaliNavamsaService kundaliNavamsaService;
    private final KundaliPublicReportAccessService accessService;

    public KundaliNavamsaController(
            KundaliNavamsaService kundaliNavamsaService,
            KundaliPublicReportAccessService accessService
    ) {
        this.kundaliNavamsaService = kundaliNavamsaService;
        this.accessService = accessService;
    }

    @GetMapping("/{reportId}/navamsa")
    public KundaliNavamsaResponse getNavamsa(@PathVariable Long reportId) {
        accessService.assertNavamsaAllowed(reportId);
        return kundaliNavamsaService.getNavamsa(reportId);
    }
}
