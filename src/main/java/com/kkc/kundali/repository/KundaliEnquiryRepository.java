package com.kkc.kundali.repository;

import com.kkc.kundali.dto.KundaliEnquiry;
import com.kkc.kundali.util.KundaliEnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KundaliEnquiryRepository extends JpaRepository<KundaliEnquiry, Long> {

    List<KundaliEnquiry> findByStatusOrderByCreatedAtDesc(KundaliEnquiryStatus status);

    List<KundaliEnquiry> findAllByOrderByCreatedAtDesc();
}