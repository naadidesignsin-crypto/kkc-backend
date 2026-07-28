package com.kkc.kundali.repository;

import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.util.KundaliReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface KundaliReportRepository extends JpaRepository<KundaliReport, Long> {

    Optional<KundaliReport> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    List<KundaliReport> findAllByOrderByCreatedAtDesc();

    Page<KundaliReport> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<KundaliReport> findByStatusOrderByCreatedAtDesc(KundaliReportStatus status);

    Page<KundaliReport> findByStatusOrderByCreatedAtDesc(
            KundaliReportStatus status,
            Pageable pageable
    );

    List<KundaliReport> findByDateOfBirthAndTimeOfBirthAndStatusOrderByCreatedAtDesc(
            LocalDate dateOfBirth,
            LocalTime timeOfBirth,
            KundaliReportStatus status
    );
}