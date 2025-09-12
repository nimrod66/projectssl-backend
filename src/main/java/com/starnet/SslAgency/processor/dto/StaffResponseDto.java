package com.starnet.SslAgency.processor.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
}
