package com.kkc.kundali.repository;

import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.util.KundaliReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select report
            from KundaliReport report
            where (:status is null or report.status = :status)
              and (
                    :search is null
                    or :search = ''
                    or lower(report.orderId) like lower(concat('%', :search, '%'))
                    or lower(report.fullName) like lower(concat('%', :search, '%'))
                    or lower(report.birthPlace) like lower(concat('%', :search, '%'))
                  )
            order by report.createdAt desc
            """)
    Page<KundaliReport> searchReports(
            @Param("status") KundaliReportStatus status,
            @Param("search") String search,
            Pageable pageable
    );
}
