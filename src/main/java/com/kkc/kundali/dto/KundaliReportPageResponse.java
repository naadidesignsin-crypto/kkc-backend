package com.kkc.kundali.dto;

import java.util.List;

public record KundaliReportPageResponse(
        List<KundaliReportListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}