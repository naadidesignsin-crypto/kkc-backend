package com.kkc.kundali.dto;

import com.kkc.kundali.util.KundaliReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "kundali_reports")
public class KundaliReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(length = 20)
    private String gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "time_of_birth", nullable = false)
    private LocalTime timeOfBirth;

    @Column(name = "birth_place", nullable = false, length = 200)
    private String birthPlace;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false, length = 80)
    private String timezone;

    @Column(length = 20)
    private String language;

    @Column(length = 80)
    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KundaliReportStatus status;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "provider_request_json")
    private String providerRequestJson;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "provider_response_json")
    private String providerResponseJson;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = KundaliReportStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
