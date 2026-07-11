package com.kkc.kundali.dto;

import com.kkc.kundali.util.KundaliEnquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KundaliStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private KundaliEnquiryStatus status;
}
