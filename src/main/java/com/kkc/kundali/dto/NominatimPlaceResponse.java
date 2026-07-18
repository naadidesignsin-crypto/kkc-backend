package com.kkc.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimPlaceResponse {

    @JsonProperty("place_id")
    private Long placeId;

    @JsonProperty("osm_type")
    private String osmType;

    @JsonProperty("osm_id")
    private Long osmId;

    @JsonProperty("display_name")
    private String displayName;

    private String lat;
    private String lon;

    @JsonProperty("class")
    private String placeClass;

    private String type;

    private Map<String, String> address;
}