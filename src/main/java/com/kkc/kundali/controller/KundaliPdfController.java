package com.kkc.kundali.controller;

import com.kkc.kundali.service.KundaliPdfService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports")
public class KundaliPdfController {

    private final KundaliPdfService pdfService;

    public KundaliPdfController(KundaliPdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/{reportId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long reportId) {
        byte[] pdfBytes = pdfService.generateReportPdf(reportId);

        String filename = "kkc-kundali-report-" + reportId + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(filename)
                        .build()
        );
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}