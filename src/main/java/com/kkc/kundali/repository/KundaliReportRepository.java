package com.kkc.kundali.repository;

import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.util.KundaliReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface KundaliReportRepository extends JpaRepository<KundaliReport, Long> {

    List<KundaliReport> findAllByOrderByCreatedAtDesc();

    List<KundaliReport> findByStatusOrderByCreatedAtDesc(KundaliReportStatus status);

    List<KundaliReport> findByDateOfBirthAndTimeOfBirthAndStatusOrderByCreatedAtDesc(
            LocalDate dateOfBirth,
            LocalTime timeOfBirth,
            KundaliReportStatus status
    );
}
