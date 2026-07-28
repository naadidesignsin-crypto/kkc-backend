package com.kkc.kundali.controller;

import com.kkc.kundali.entity.KundaliReport;
import com.kkc.kundali.service.KundaliPdfService;
import com.kkc.kundali.service.KundaliPublicReportAccessService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kundali/reports")
public class KundaliPdfController {

    private final KundaliPdfService pdfService;
    private final KundaliPublicReportAccessService accessService;

    public KundaliPdfController(
            KundaliPdfService pdfService,
            KundaliPublicReportAccessService accessService
    ) {
        this.pdfService = pdfService;
        this.accessService = accessService;
    }

    @GetMapping("/{reportId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long reportId,
            @RequestParam String orderId
    ) {
        KundaliReport report = accessService.requireReportForOrder(reportId, orderId);
        accessService.requirePdfAccess(report);

        byte[] pdfBytes = pdfService.generateReportPdf(reportId);

        String filename = "kkc-kundali-report-" + report.getOrderId() + ".pdf";
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
