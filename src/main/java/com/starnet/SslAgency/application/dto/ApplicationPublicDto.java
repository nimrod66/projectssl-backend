package com.starnet.SslAgency.application.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationPublicDto {
    private Long id;
    private String fullName;
    private Integer age;
    private String nationality;
    private String experience;
    private String currentLocation;
    private List<String> languages;

    private List<String> videos;
    private List<String> showcasePhotos;

    private Boolean hasCat;
    private Boolean hasDog;
    private Boolean extraPay;
    private Boolean liveOut;
    private Boolean privateRoom;
    private Boolean elderlyCare;
    private Boolean specialNeeds;
    private Boolean olderThan1;
    private Boolean youngerThan1;


}
