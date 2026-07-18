package com.kkc.kundali.dto;

import com.kkc.kundali.util.KundaliReportSectionType;
import com.kkc.kundali.util.KundaliReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "kundali_report_sections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_kundali_report_section",
                        columnNames = {"report_id", "section_type"}
                )
        }
)
public class KundaliReportSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 60)
    private KundaliReportSectionType sectionType;

    @Column(nullable = false, length = 60)
    private String provider;

    @Column(name = "provider_endpoint", length = 255)
    private String providerEndpoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private KundaliReportStatus status;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "request_json")
    private String requestJson;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "response_json")
    private String responseJson;

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