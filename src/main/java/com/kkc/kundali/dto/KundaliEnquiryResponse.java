package com.kkc.kundali.dto;

import com.kkc.kundali.entity.KundaliEnquiry;
import com.kkc.kundali.util.ConsultationType;
import com.kkc.kundali.util.KundaliEnquiryStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KundaliEnquiryResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalTime timeOfBirth;
    private String birthPlace;
    private ConsultationType consultationType;
    private String question;
    private KundaliEnquiryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static KundaliEnquiryResponse from(KundaliEnquiry enquiry) {
        return KundaliEnquiryResponse.builder()
                .id(enquiry.getId())
                .fullName(enquiry.getFullName())
                .phone(enquiry.getPhone())
                .email(enquiry.getEmail())
                .gender(enquiry.getGender())
                .dateOfBirth(enquiry.getDateOfBirth())
                .timeOfBirth(enquiry.getTimeOfBirth())
                .birthPlace(enquiry.getBirthPlace())
                .consultationType(enquiry.getConsultationType())
                .question(enquiry.getQuestion())
                .status(enquiry.getStatus())
                .createdAt(enquiry.getCreatedAt())
                .updatedAt(enquiry.getUpdatedAt())
                .build();
    }
}