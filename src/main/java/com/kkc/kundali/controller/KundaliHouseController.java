package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliHouseResponse;
import com.kkc.kundali.service.KundaliHouseService;
import com.kkc.kundali.service.KundaliPublicReportAccessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports")
public class KundaliHouseController {

    private final KundaliHouseService kundaliHouseService;
    private final KundaliPublicReportAccessService accessService;

    public KundaliHouseController(
            KundaliHouseService kundaliHouseService,
            KundaliPublicReportAccessService accessService
    ) {
        this.kundaliHouseService = kundaliHouseService;
        this.accessService = accessService;
    }

    @GetMapping("/{reportId}/houses")
    public KundaliHouseResponse getHouseInterpretations(@PathVariable Long reportId) {
        accessService.assertHousesAllowed(reportId);
        return kundaliHouseService.getHouseInterpretations(reportId);
    }
}
