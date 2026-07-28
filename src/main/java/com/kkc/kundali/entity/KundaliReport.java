package com.kkc.kundali.entity;

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
@Table(
        name = "kundali_reports",
        indexes = {
                @Index(name = "idx_kundali_reports_order_id", columnList = "order_id"),
                @Index(name = "idx_kundali_reports_status", columnList = "status"),
                @Index(name = "idx_kundali_reports_created_at", columnList = "created_at")
        }
)
public class KundaliReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 80)
    private String orderId;

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

    @Column(name = "ascendant", length = 80)
    private String ascendant;

    @Column(name = "rashi", length = 80)
    private String rashi;

    @Column(name = "sign_lord", length = 80)
    private String signLord;

    @Column(name = "nakshatra", length = 80)
    private String nakshatra;

    @Column(name = "nakshatra_lord", length = 80)
    private String nakshatraLord;

    @Column(name = "current_dasha", length = 80)
    private String currentDasha;

    @Builder.Default
    @Column(name = "show_summary", nullable = false)
    private Boolean showSummary = true;

    @Builder.Default
    @Column(name = "show_consultation", nullable = false)
    private Boolean showConsultation = true;

    @Builder.Default
    @Column(name = "show_birth_chart", nullable = false)
    private Boolean showBirthChart = false;

    @Builder.Default
    @Column(name = "show_planets", nullable = false)
    private Boolean showPlanets = false;

    @Builder.Default
    @Column(name = "show_houses", nullable = false)
    private Boolean showHouses = false;

    @Builder.Default
    @Column(name = "show_navamsa", nullable = false)
    private Boolean showNavamsa = false;

    @Builder.Default
    @Column(name = "show_parashara", nullable = false)
    private Boolean showParashara = false;

    @Builder.Default
    @Column(name = "show_dasha", nullable = false)
    private Boolean showDasha = false;

    @Builder.Default
    @Column(name = "show_dosha", nullable = false)
    private Boolean showDosha = false;

    @Builder.Default
    @Column(name = "show_pdf", nullable = false)
    private Boolean showPdf = false;

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

        applyAccessDefaults();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        applyAccessDefaults();
    }

    private void applyAccessDefaults() {
        if (this.showSummary == null) this.showSummary = true;
        if (this.showConsultation == null) this.showConsultation = true;
        if (this.showBirthChart == null) this.showBirthChart = false;
        if (this.showPlanets == null) this.showPlanets = false;
        if (this.showHouses == null) this.showHouses = false;
        if (this.showNavamsa == null) this.showNavamsa = false;
        if (this.showParashara == null) this.showParashara = false;
        if (this.showDasha == null) this.showDasha = false;
        if (this.showDosha == null) this.showDosha = false;
        if (this.showPdf == null) this.showPdf = false;
    }
}
