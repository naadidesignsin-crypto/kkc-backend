package com.kkc.kundali.dto;

import com.kkc.kundali.util.KundaliConsultationStatus;
import jakarta.validation.constraints.NotNull;

public record KundaliConsultationStatusUpdateRequest(
        @NotNull(message = "Status is required")
        KundaliConsultationStatus status
) {
}
