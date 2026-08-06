package com.kkc.kundali.controller;

import com.kkc.kundali.dto.DailyPanchangResponse;
import com.kkc.kundali.service.DailyPanchangService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/panchang")
public class DailyPanchangController {

    private final DailyPanchangService dailyPanchangService;

    public DailyPanchangController(DailyPanchangService dailyPanchangService) {
        this.dailyPanchangService = dailyPanchangService;
    }

    @GetMapping("/daily")
    public DailyPanchangResponse getDailyPanchang(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(defaultValue = "hyderabad") String place
    ) {
        return dailyPanchangService.getDailyPanchang(date, place);
    }

    @GetMapping("/places")
    public List<DailyPanchangResponse.DailyPanchangPlaceOption> getPlaces() {
        return dailyPanchangService.getSupportedPlaces();
    }
}
