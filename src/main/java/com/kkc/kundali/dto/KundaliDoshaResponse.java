package com.kkc.kundali.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KundaliDoshaResponse {

    private Long reportId;
    private String sectionType;
    private String status;

    private Boolean mangalDoshaPresent;
    private String type;
    private String intensity;
    private String reason;
    private String info;
}