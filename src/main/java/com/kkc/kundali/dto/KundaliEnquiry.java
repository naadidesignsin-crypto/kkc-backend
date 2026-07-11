package com.kkc.kundali.dto;


import com.kkc.kundali.util.ConsultationType;
import com.kkc.kundali.util.KundaliEnquiryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "kundali_enquiries")
public class KundaliEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "time_of_birth", nullable = false)
    private LocalTime timeOfBirth;

    @Column(name = "birth_place", nullable = false, length = 200)
    private String birthPlace;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_type", nullable = false, length = 60)
    private ConsultationType consultationType;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KundaliEnquiryStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = KundaliEnquiryStatus.NEW;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}