package com.kkc.kundali.repository;

import com.kkc.kundali.entity.KundaliReportSection;
import com.kkc.kundali.util.KundaliReportSectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KundaliReportSectionRepository extends JpaRepository<KundaliReportSection, Long> {

    List<KundaliReportSection> findByReportIdOrderByCreatedAtAsc(Long reportId);

    Optional<KundaliReportSection> findByReportIdAndSectionType(
            Long reportId,
            KundaliReportSectionType sectionType
    );
}
