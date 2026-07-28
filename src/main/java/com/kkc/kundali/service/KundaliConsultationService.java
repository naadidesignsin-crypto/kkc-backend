package com.kkc.kundali.service;

import com.kkc.kundali.dto.KundaliConsultationCreateRequest;
import com.kkc.kundali.dto.KundaliConsultationResponse;
import com.kkc.kundali.entity.KundaliConsultationRequest;
import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.repository.KundaliConsultationRequestRepository;
import com.kkc.kundali.repository.KundaliReportRepository;
import com.kkc.kundali.util.KundaliConsultationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class KundaliConsultationService {

    private final KundaliReportRepository reportRepository;
    private final KundaliConsultationRequestRepository consultationRepository;
    private final String whatsappNumber;

    public KundaliConsultationService(
            KundaliReportRepository reportRepository,
            KundaliConsultationRequestRepository consultationRepository,
            @Value("${app.whatsapp.number:919700051668}") String whatsappNumber
    ) {
        this.reportRepository = reportRepository;
        this.consultationRepository = consultationRepository;
        this.whatsappNumber = normalizePhoneNumber(whatsappNumber);
    }

    @Transactional
    public KundaliConsultationResponse createConsultation(
            String orderId,
            KundaliConsultationCreateRequest request
    ) {
        KundaliReport report = findReportByOrderId(orderId);
        String sectionName = cleanSectionName(request != null ? request.sectionName() : null);
        String message = buildWhatsappMessage(report, sectionName);
        String whatsappUrl = buildWhatsappUrl(message);

        KundaliConsultationRequest consultation = KundaliConsultationRequest.builder()
                .report(report)
                .orderId(report.getOrderId())
                .fullName(report.getFullName())
                .gender(report.getGender())
                .birthPlace(report.getBirthPlace())
                .dateOfBirth(value(report.getDateOfBirth()))
                .timeOfBirth(value(report.getTimeOfBirth()))
                .sectionName(sectionName)
                .whatsappNumber(whatsappNumber)
                .whatsappUrl(whatsappUrl)
                .whatsappMessage(message)
                .status(KundaliConsultationStatus.WHATSAPP_MESSAGE_CREATED)
                .build();

        return KundaliConsultationResponse.from(
                consultationRepository.save(consultation)
        );
    }

    @Transactional(readOnly = true)
    public KundaliConsultationResponse findLatestByOrderId(String orderId) {
        String normalizedOrderId = normalizeOrderId(orderId);

        KundaliConsultationRequest consultation = consultationRepository
                .findFirstByOrderIdOrderByCreatedAtDesc(normalizedOrderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No consultation message found for this Order ID."
                ));

        return KundaliConsultationResponse.from(consultation);
    }

    @Transactional(readOnly = true)
    public Page<KundaliConsultationResponse> listConsultations(
            String query,
            Pageable pageable
    ) {
        Page<KundaliConsultationRequest> page;

        if (query == null || query.isBlank()) {
            page = consultationRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            String cleanQuery = query.trim();
            page = consultationRepository
                    .findByOrderIdContainingIgnoreCaseOrFullNameContainingIgnoreCaseOrBirthPlaceContainingIgnoreCaseOrderByCreatedAtDesc(
                            cleanQuery,
                            cleanQuery,
                            cleanQuery,
                            pageable
                    );
        }

        return page.map(KundaliConsultationResponse::from);
    }

    @Transactional(readOnly = true)
    public KundaliConsultationResponse findById(Long id) {
        KundaliConsultationRequest consultation = consultationRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Consultation request not found."
                ));

        return KundaliConsultationResponse.from(consultation);
    }

    @Transactional
    public KundaliConsultationResponse updateStatus(
            Long id,
            KundaliConsultationStatus status
    ) {
        KundaliConsultationRequest consultation = consultationRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Consultation request not found."
                ));

        consultation.setStatus(status);

        return KundaliConsultationResponse.from(
                consultationRepository.save(consultation)
        );
    }

    private KundaliReport findReportByOrderId(String orderId) {
        String normalizedOrderId = normalizeOrderId(orderId);

        return reportRepository.findByOrderId(normalizedOrderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kundali report not found for this Order ID."
                ));
    }

    private String buildWhatsappMessage(
            KundaliReport report,
            String sectionName
    ) {
        return String.join(
                "\n",
                "Namaste KKC, I want to book an astrology consultation.",
                "",
                "Generated Kundali Details:",
                "Order ID: " + safe(report.getOrderId()),
                "Report ID: " + safe(report.getId()),
                "Name: " + safe(report.getFullName()),
                "Gender: " + safe(report.getGender()),
                "Date of Birth: " + safe(report.getDateOfBirth()),
                "Time of Birth: " + safe(report.getTimeOfBirth()),
                "Birth Place: " + safe(report.getBirthPlace()),
                "",
                "Basic Birth Summary:",
                "Ascendant / Lagna: " + safe(report.getAscendant()),
                "Rashi: " + safe(report.getRashi()),
                "Sign Lord: " + safe(report.getSignLord()),
                "Nakshatra: " + safe(report.getNakshatra()),
                "Nakshatra Lord: " + safe(report.getNakshatraLord()),
                "Provider: " + safe(report.getProvider()),
                "",
                "Requested Section: " + safe(sectionName),
                "",
                "Please guide me based on this Kundali report."
        );
    }

    private String buildWhatsappUrl(String message) {
        String encodedMessage = URLEncoder.encode(
                message,
                StandardCharsets.UTF_8
        );

        return "https://wa.me/" + whatsappNumber + "?text=" + encodedMessage;
    }

    private String normalizeOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order ID is required."
            );
        }

        return orderId.trim().toUpperCase();
    }

    private String cleanSectionName(String sectionName) {
        if (sectionName == null || sectionName.isBlank()) {
            return "General Consultation";
        }

        return sectionName.trim();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return "919700051668";
        }

        return phoneNumber.replaceAll("[^0-9]", "");
    }

    private String value(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String safe(Object value) {
        if (value == null) {
            return "-";
        }

        String text = String.valueOf(value).trim();
        return text.isBlank() ? "-" : text;
    }
}
