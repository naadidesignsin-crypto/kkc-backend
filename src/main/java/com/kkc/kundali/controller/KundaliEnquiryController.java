package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliEnquiryRequest;
import com.kkc.kundali.dto.KundaliEnquiryResponse;
import com.kkc.kundali.service.KundaliEnquiryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/enquiries")
public class KundaliEnquiryController {

    private final KundaliEnquiryService service;

    public KundaliEnquiryController(KundaliEnquiryService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KundaliEnquiryResponse create(@Valid @RequestBody KundaliEnquiryRequest request) {
        return service.create(request);
    }
}