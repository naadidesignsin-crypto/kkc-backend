package com.kkc.kundali.dto;

import jakarta.validation.constraints.Size;

public record KundaliConsultationCreateRequest(
        @Size(max = 120, message = "Section name must be 120 characters or less")
        String sectionName
) {
}
