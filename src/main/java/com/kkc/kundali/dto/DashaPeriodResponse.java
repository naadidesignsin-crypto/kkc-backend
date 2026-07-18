package com.kkc.kundali.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashaPeriodResponse {

    private String planet;
    private String startDate;
    private String endDate;
    private Boolean active;
}