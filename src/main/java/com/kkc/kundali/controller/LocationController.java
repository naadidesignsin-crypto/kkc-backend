package com.kkc.location.controller;

import com.kkc.location.dto.LocationSearchResponse;
import com.kkc.location.service.LocationSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationSearchService locationSearchService;

    public LocationController(LocationSearchService locationSearchService) {
        this.locationSearchService = locationSearchService;
    }

    @GetMapping("/search")
    public List<LocationSearchResponse> searchLocations(
            @RequestParam String query,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return locationSearchService.search(query, limit);
    }
}