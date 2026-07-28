package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliDashaResponse;
import com.kkc.kundali.dto.KundaliDoshaResponse;
import com.kkc.kundali.dto.KundaliPlanetsResponse;
import com.kkc.kundali.service.KundaliDisplayService;
import com.kkc.kundali.service.KundaliPublicReportAccessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports/{reportId}")
public class KundaliDisplayController {

    private final KundaliDisplayService service;
    private final KundaliPublicReportAccessService accessService;

    public KundaliDisplayController(
            KundaliDisplayService service,
            KundaliPublicReportAccessService accessService
    ) {
        this.service = service;
        this.accessService = accessService;
    }

    @GetMapping("/planets")
    public KundaliPlanetsResponse getPlanets(@PathVariable Long reportId) {
        accessService.assertPlanetsAllowed(reportId);
        return service.getPlanets(reportId);
    }

    @GetMapping("/dasha")
    public KundaliDashaResponse getDasha(@PathVariable Long reportId) {
        accessService.assertDashaAllowed(reportId);
        return service.getDasha(reportId);
    }

    @GetMapping("/dosha")
    public KundaliDoshaResponse getDosha(@PathVariable Long reportId) {
        accessService.assertDoshaAllowed(reportId);
        return service.getDosha(reportId);
    }
}
