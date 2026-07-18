package com.kkc.kundali.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderResult {

    private String requestJson;
    private String responseJson;
}