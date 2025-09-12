package com.starnet.SslAgency.processor.dto;

import com.starnet.SslAgency.processor.model.Staff;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffRequestDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @Pattern(regexp = "^(\\+254|0)(7\\d{8}|1\\d{8})$", message = "Invalid Kenyan phone number")
    private String phoneNumber;
    @NotNull
    private Staff.Role role;
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
