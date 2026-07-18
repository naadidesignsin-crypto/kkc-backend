package com.kkc;

import com.kkc.kundali.dto.KundliApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(KundliApiProperties.class)
public class KkcBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(KkcBackendApplication.class, args);
    }
}
