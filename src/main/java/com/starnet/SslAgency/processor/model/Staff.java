package com.starnet.SslAgency.processor.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "staff", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Pattern(regexp = "^(\\+254|0)(7\\d{8}|1\\d{8})$", message = "Invalid Kenyan phone number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Column(nullable = false)
    private String passwordHash;

    public enum Role {
        SUPER_ADMIN, ADMIN, RECEPTIONIST
    }

}
