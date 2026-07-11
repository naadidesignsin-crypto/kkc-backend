package com.kkc.kundali.dto;

import com.kkc.kundali.util.ConsultationType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KundaliEnquiryRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name must be within 120 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid 10-digit Indian mobile number"
    )
    private String phone;

    @Email(message = "Enter a valid email")
    @Size(max = 150, message = "Email must be within 150 characters")
    private String email;

    @Size(max = 20, message = "Gender must be within 20 characters")
    private String gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Time of birth is required")
    private LocalTime timeOfBirth;

    @NotBlank(message = "Birth place is required")
    @Size(max = 200, message = "Birth place must be within 200 characters")
    private String birthPlace;

    @NotNull(message = "Consultation type is required")
    private ConsultationType consultationType;

    @Size(max = 2000, message = "Question must be within 2000 characters")
    private String question;
}
