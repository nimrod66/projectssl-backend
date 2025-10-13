package com.starnet.SslAgency.interapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InterApplicationPublicDto {
    private Long id;
    private String fullName;
    private Integer age;
    private String nationality;
    private String experience;
    private String currentLocation;
    private List<String> languages;

    private List<String> videos;
    private List<String> showcasePhotos;
}



