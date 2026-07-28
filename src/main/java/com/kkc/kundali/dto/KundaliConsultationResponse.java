package com.kkc.kundali.dto;

import com.kkc.kundali.entity.KundaliConsultationRequest;
import com.kkc.kundali.util.KundaliConsultationStatus;

import java.time.LocalDateTime;

public record KundaliConsultationResponse(
        Long consultationId,
        Long reportId,
        String orderId,
        String fullName,
        String gender,
        String birthPlace,
        String dateOfBirth,
        String timeOfBirth,
        String sectionName,
        String whatsappNumber,
        String whatsappUrl,
        String whatsappMessage,
        KundaliConsultationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static KundaliConsultationResponse from(
            KundaliConsultationRequest request
    ) {
        return new KundaliConsultationResponse(
                request.getId(),
                request.getReport() != null ? request.getReport().getId() : null,
                request.getOrderId(),
                request.getFullName(),
                request.getGender(),
                request.getBirthPlace(),
                request.getDateOfBirth(),
                request.getTimeOfBirth(),
                request.getSectionName(),
                request.getWhatsappNumber(),
                request.getWhatsappUrl(),
                request.getWhatsappMessage(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
