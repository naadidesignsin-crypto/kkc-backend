package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliHouseResponse;
import com.kkc.kundali.service.KundaliHouseService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports")
public class KundaliHouseController {

    private final KundaliHouseService kundaliHouseService;

    public KundaliHouseController(KundaliHouseService kundaliHouseService) {
        this.kundaliHouseService = kundaliHouseService;
    }

    @GetMapping("/{reportId}/houses")
    public KundaliHouseResponse getHouseInterpretations(@PathVariable Long reportId) {
        return kundaliHouseService.getHouseInterpretations(reportId);
    }
}