package com.kkc.kundali.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kundli-api")
public class KundliApiProperties {

    private String baseUrl;
    private String apiKey;
    private String astroDataPath;
}
