package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliReportResponse;
import com.kkc.kundali.service.KundaliGenerationService;
import com.kkc.kundali.util.KundaliReportStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/kundali/reports")
public class AdminKundaliReportController {

    private final KundaliGenerationService service;

    public AdminKundaliReportController(KundaliGenerationService service) {
        this.service = service;
    }

    @GetMapping
    public List<KundaliReportResponse> findAll(
            @RequestParam(required = false) KundaliReportStatus status
    ) {
        return service.findAll(status);
    }
}
