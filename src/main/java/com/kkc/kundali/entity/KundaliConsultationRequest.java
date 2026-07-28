package com.kkc.kundali.entity;

import com.kkc.kundali.util.KundaliConsultationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kundali_consultation_requests")
public class KundaliConsultationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private KundaliReport report;

    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Column(name = "full_name", length = 160)
    private String fullName;

    @Column(name = "gender", length = 30)
    private String gender;

    @Column(name = "birth_place", length = 320)
    private String birthPlace;

    @Column(name = "date_of_birth", length = 32)
    private String dateOfBirth;

    @Column(name = "time_of_birth", length = 32)
    private String timeOfBirth;

    @Column(name = "section_name", length = 120)
    private String sectionName;

    @Column(name = "whatsapp_number", nullable = false, length = 32)
    private String whatsappNumber;

    @Column(name = "whatsapp_url", nullable = false, length = 2048)
    private String whatsappUrl;

    @Column(name = "whatsapp_message", nullable = false, columnDefinition = "TEXT")
    private String whatsappMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private KundaliConsultationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = KundaliConsultationStatus.WHATSAPP_MESSAGE_CREATED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
