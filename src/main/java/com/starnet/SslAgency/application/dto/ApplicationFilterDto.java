package com.starnet.SslAgency.application.dto;

import com.starnet.SslAgency.application.model.Application;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationFilterDto {
    private Boolean hasCat;
    private Boolean hasDog;
    private Boolean extraPay;
    private Boolean liveOut;
    private Boolean privateRoom;
    private Boolean elderlyCare;
    private Boolean specialNeeds;
    private Boolean olderThan1;
    private Boolean youngerThan1;

    private String currentLocation;
}


