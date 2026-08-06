package com.kkc.kundali.controller;

import com.kkc.kundali.dto.DailyRasiResponse;
import com.kkc.kundali.service.DailyRasiService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class DailyRasiController {

    private final DailyRasiService dailyRasiService;

    public DailyRasiController(DailyRasiService dailyRasiService) {
        this.dailyRasiService = dailyRasiService;
    }

    @GetMapping("/api/rasi/daily")
    public DailyRasiResponse getDailyRasi(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false, defaultValue = "mesha") String rasi,
            @RequestParam(required = false, defaultValue = "hyderabad") String place
    ) {
        return dailyRasiService.getDailyRasi(date, rasi, place);
    }
}
