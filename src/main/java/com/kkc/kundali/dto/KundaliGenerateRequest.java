package com.kkc.kundali.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KundaliGenerateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 120)
    private String fullName;

    @Size(max = 20)
    private String gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Time of birth is required")
    private LocalTime timeOfBirth;

    @NotBlank(message = "Birth place is required")
    @Size(max = 200)
    private String birthPlace;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @NotBlank(message = "Timezone is required")
    private String timezone;

    @Builder.Default
    private String language = "en";
}