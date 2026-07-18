package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliNavamsaResponse;
import com.kkc.kundali.service.KundaliNavamsaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports")
public class KundaliNavamsaController {

    private final KundaliNavamsaService kundaliNavamsaService;

    public KundaliNavamsaController(KundaliNavamsaService kundaliNavamsaService) {
        this.kundaliNavamsaService = kundaliNavamsaService;
    }

    @GetMapping("/{reportId}/navamsa")
    public KundaliNavamsaResponse getNavamsa(@PathVariable Long reportId) {
        return kundaliNavamsaService.getNavamsa(reportId);
    }
}