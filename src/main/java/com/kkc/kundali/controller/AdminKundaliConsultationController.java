package com.kkc.kundali.controller;

import com.kkc.kundali.dto.KundaliConsultationResponse;
import com.kkc.kundali.dto.KundaliConsultationStatusUpdateRequest;
import com.kkc.kundali.service.KundaliConsultationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/kundali/consultations")
public class AdminKundaliConsultationController {

    private final KundaliConsultationService consultationService;

    public AdminKundaliConsultationController(
            KundaliConsultationService consultationService
    ) {
        this.consultationService = consultationService;
    }

    @GetMapping
    public Page<KundaliConsultationResponse> listConsultations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return consultationService.listConsultations(query, pageable);
    }

    @GetMapping("/{id}")
    public KundaliConsultationResponse getConsultation(
            @PathVariable Long id
    ) {
        return consultationService.findById(id);
    }

    @PatchMapping("/{id}/status")
    public KundaliConsultationResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody KundaliConsultationStatusUpdateRequest request
    ) {
        return consultationService.updateStatus(id, request.status());
    }
}
