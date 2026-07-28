package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliConsultationCreateRequest;
import com.kkc.kundali.dto.KundaliConsultationResponse;
import com.kkc.kundali.service.KundaliConsultationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kundali/order/{orderId}/consultation")
public class KundaliConsultationController {

    private final KundaliConsultationService consultationService;

    public KundaliConsultationController(
            KundaliConsultationService consultationService
    ) {
        this.consultationService = consultationService;
    }

    @PostMapping
    public KundaliConsultationResponse createConsultation(
            @PathVariable String orderId,
            @Valid @RequestBody(required = false)
            KundaliConsultationCreateRequest request
    ) {
        return consultationService.createConsultation(orderId, request);
    }

    @GetMapping("/latest")
    public KundaliConsultationResponse getLatestConsultation(
            @PathVariable String orderId
    ) {
        return consultationService.findLatestByOrderId(orderId);
    }
}
