package com.kkc.kundali.util;

public enum KundaliReportSectionType {

    BASIC_ASTRO("/api/astro/get_astro_data"),
    PLANETARY_POSITIONS("/api/planet/get_all_planet_data"),
    DASHA("/api/dasha/get_vimshottary_maha_dasha"),
    DOSHA("/api/dosha/mangal_dosh_analysis"),

    RASHI_CHART(null),
    NAVAMSA_CHART(null),
    HOUSE_PLACEMENTS(null),
    KUNDALI_MATCHING(null),
    PDF_REPORT(null);

    private final String endpointPath;

    KundaliReportSectionType(String endpointPath) {
        this.endpointPath = endpointPath;
    }

    public String getEndpointPath() {
        return endpointPath;
    }

    public boolean isSupportedNow() {
        return endpointPath != null && !endpointPath.isBlank();
    }
}