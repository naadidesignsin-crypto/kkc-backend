package com.kkc.kundali.service;


import com.kkc.kundali.dto.KundaliEnquiry;
import com.kkc.kundali.dto.KundaliEnquiryRequest;
import com.kkc.kundali.dto.KundaliEnquiryResponse;
import com.kkc.kundali.dto.KundaliStatusUpdateRequest;
import com.kkc.kundali.repository.KundaliEnquiryRepository;
import com.kkc.kundali.util.KundaliEnquiryStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KundaliEnquiryService {

    private final KundaliEnquiryRepository repository;

    public KundaliEnquiryService(KundaliEnquiryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public KundaliEnquiryResponse create(KundaliEnquiryRequest request) {
        KundaliEnquiry enquiry = KundaliEnquiry.builder()
                .fullName(normalizeRequired(request.getFullName()))
                .phone(normalizeRequired(request.getPhone()))
                .email(normalizeOptional(request.getEmail()))
                .gender(normalizeOptional(request.getGender()))
                .dateOfBirth(request.getDateOfBirth())
                .timeOfBirth(request.getTimeOfBirth())
                .birthPlace(normalizeRequired(request.getBirthPlace()))
                .consultationType(request.getConsultationType())
                .question(normalizeOptional(request.getQuestion()))
                .status(KundaliEnquiryStatus.NEW)
                .build();

        KundaliEnquiry saved = repository.save(enquiry);
        return KundaliEnquiryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<KundaliEnquiryResponse> findAll(KundaliEnquiryStatus status) {
        List<KundaliEnquiry> enquiries = status == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status);

        return enquiries.stream()
                .map(KundaliEnquiryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public KundaliEnquiryResponse findById(Long id) {
        KundaliEnquiry enquiry = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kundali enquiry not found"));

        return KundaliEnquiryResponse.from(enquiry);
    }

    @Transactional
    public KundaliEnquiryResponse updateStatus(Long id, KundaliStatusUpdateRequest request) {
        KundaliEnquiry enquiry = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kundali enquiry not found"));

        enquiry.setStatus(request.getStatus());

        KundaliEnquiry saved = repository.save(enquiry);
        return KundaliEnquiryResponse.from(saved);
    }

    private String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
