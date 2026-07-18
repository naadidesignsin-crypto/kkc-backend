package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliDashaResponse;
import com.kkc.kundali.dto.KundaliDoshaResponse;
import com.kkc.kundali.dto.KundaliPlanetsResponse;
import com.kkc.kundali.service.KundaliDisplayService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports/{reportId}")
public class KundaliDisplayController {

    private final KundaliDisplayService service;

    public KundaliDisplayController(KundaliDisplayService service) {
        this.service = service;
    }

    @GetMapping("/planets")
    public KundaliPlanetsResponse getPlanets(@PathVariable Long reportId) {
        return service.getPlanets(reportId);
    }

    @GetMapping("/dasha")
    public KundaliDashaResponse getDasha(@PathVariable Long reportId) {
        return service.getDasha(reportId);
    }

    @GetMapping("/dosha")
    public KundaliDoshaResponse getDosha(@PathVariable Long reportId) {
        return service.getDosha(reportId);
    }
}