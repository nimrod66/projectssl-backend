package com.starnet.SslAgency.application.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationSearchDto {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private Integer age;
    private String passportPhotoUrl;
}
