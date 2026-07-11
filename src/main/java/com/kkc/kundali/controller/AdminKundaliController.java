package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliEnquiryResponse;
import com.kkc.kundali.dto.KundaliStatusUpdateRequest;
import com.kkc.kundali.service.KundaliEnquiryService;
import com.kkc.kundali.util.KundaliEnquiryStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/kundali/enquiries")
public class AdminKundaliController {

    private final KundaliEnquiryService service;

    public AdminKundaliController(KundaliEnquiryService service) {
        this.service = service;
    }

    @GetMapping
    public List<KundaliEnquiryResponse> findAll(
            @RequestParam(required = false) KundaliEnquiryStatus status
    ) {
        return service.findAll(status);
    }

    @GetMapping("/{id}")
    public KundaliEnquiryResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/status")
    public KundaliEnquiryResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody KundaliStatusUpdateRequest request
    ) {
        return service.updateStatus(id, request);
    }
}
