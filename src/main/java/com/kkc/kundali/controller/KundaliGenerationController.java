package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliGenerateRequest;
import com.kkc.kundali.dto.KundaliReportPageResponse;
import com.kkc.kundali.dto.KundaliReportResponse;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.service.KundaliGenerationService;
import com.kkc.kundali.util.KundaliReportStatus;
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

    @GetMapping("/reports")
    public KundaliReportPageResponse findGeneratedReports(
            @RequestParam(required = false) KundaliReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.findGeneratedReports(status, page, size);
    }

    @GetMapping("/reports/{id}")
    public KundaliReportResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/reports/{id}/summary")
    public KundaliSummaryResponse findSummaryById(@PathVariable Long id) {
        return service.findSummaryById(id);
    }
}