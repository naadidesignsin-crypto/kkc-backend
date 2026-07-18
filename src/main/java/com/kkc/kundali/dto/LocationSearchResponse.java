package com.kkc.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearchResponse {

    private String id;

    private String displayName;
    private String birthPlace;

    private Double latitude;
    private Double longitude;
    private String timezone;

    private String city;
    private String state;
    private String country;
    private String countryCode;

    private String source;
}