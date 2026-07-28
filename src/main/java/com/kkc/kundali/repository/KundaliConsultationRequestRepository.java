package com.kkc.kundali.repository;

import com.kkc.kundali.entity.KundaliConsultationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KundaliConsultationRequestRepository
        extends JpaRepository<KundaliConsultationRequest, Long> {

    Optional<KundaliConsultationRequest> findFirstByOrderIdOrderByCreatedAtDesc(
            String orderId
    );

    Page<KundaliConsultationRequest> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    Page<KundaliConsultationRequest>
    findByOrderIdContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrBirthPlaceContainingIgnoreCaseOrderByCreatedAtDesc(
            String orderId,
            String fullName,
            String birthPlace,
            Pageable pageable
    );
}
