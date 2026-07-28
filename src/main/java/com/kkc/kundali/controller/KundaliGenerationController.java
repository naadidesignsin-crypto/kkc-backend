package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliGenerateRequest;
import com.kkc.kundali.dto.KundaliReportResponse;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.service.KundaliGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali")
public class KundaliGenerationController {

    private final KundaliGenerationService service;

    public KundaliGenerationController(KundaliGenerationService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public KundaliReportResponse generate(@Valid @RequestBody KundaliGenerateRequest request) {
        return service.generate(request);
    }

    /*
     * Public non-admin lookup.
     * User must know exact generated Order ID.
     * This returns summary + access flags only, not admin list data.
     */
    @GetMapping("/orders/{orderId}/summary")
    public KundaliSummaryResponse findSummaryByOrderId(@PathVariable String orderId) {
        return service.findSummaryByOrderId(orderId);
    }

    /*
     * Used immediately after generation because frontend receives internal report id.
     * Still returns only summary + access flags.
     */
    @GetMapping("/reports/{id}/summary")
    public KundaliSummaryResponse findSummaryById(@PathVariable Long id) {
        return service.findSummaryById(id);
    }
}
