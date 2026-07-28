package com.kkc.kundali.dto;

import java.util.List;

public record AdminKundaliReportPageResponse(
        List<AdminKundaliReportListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
