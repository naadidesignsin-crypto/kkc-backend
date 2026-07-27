package com.kkc.kundali.controller;

import com.kkc.kundali.dto.DevDataResetResponse;
import com.kkc.kundali.service.DevDataResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dev")
public class DevDataResetController {

    private static final String CONFIRMATION_TEXT = "DELETE_KUNDALI_DATA";

    private final DevDataResetService devDataResetService;

    @Value("${dev-reset.enabled:false}")
    private boolean devResetEnabled;

    @Value("${dev-reset.secret:}")
    private String devResetSecret;

    public DevDataResetController(DevDataResetService devDataResetService) {
        this.devDataResetService = devDataResetService;
    }

    @PostMapping("/kundali/reports/delete-rows")
    @ResponseStatus(HttpStatus.OK)
    public DevDataResetResponse deleteKundaliReportRows(
            @RequestHeader(value = "X-DEV-RESET-SECRET", required = false) String secret,
            @RequestParam String confirm,
            @RequestParam(defaultValue = "false") boolean resetIds
    ) {
        validateResetAccess(secret, confirm);

        return devDataResetService.deleteKundaliReportRows(resetIds);
    }

    private void validateResetAccess(String secret, String confirm) {
        if (!devResetEnabled) {
            throw new IllegalStateException("Dev reset endpoint is disabled.");
        }

        if (devResetSecret == null || devResetSecret.isBlank()) {
            throw new IllegalStateException("Dev reset secret is not configured.");
        }

        if (secret == null || !devResetSecret.equals(secret)) {
            throw new SecurityException("Invalid reset secret.");
        }

        if (!CONFIRMATION_TEXT.equals(confirm)) {
            throw new IllegalArgumentException(
                    "Invalid confirmation text. Use confirm=" + CONFIRMATION_TEXT
            );
        }
    }
}