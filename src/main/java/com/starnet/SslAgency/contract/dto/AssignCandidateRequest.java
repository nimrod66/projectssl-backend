package com.starnet.SslAgency.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignCandidateRequest {
    @NotBlank
    private String candidateType;
    @NotNull
    private Long candidateId;
}
