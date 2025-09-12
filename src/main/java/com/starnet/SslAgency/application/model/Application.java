package com.starnet.SslAgency.application.model;


import com.starnet.SslAgency.media.model.MediaFile;
import com.starnet.SslAgency.processor.model.Staff;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity

@Table(name = "applicants")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Applicants info
    @NotBlank
    private String firstName;
    private String middleName;
    @NotBlank
    private String lastName;

    @Pattern(regexp = "^(\\+254|0)(7\\d{8}|1\\d{8})$", message = "Invalid Kenyan phone number")
    private String phoneNumber;
    @Email
    @NotBlank
    private String email;

    @Past
    private LocalDate dob;

    private Integer age;

    private String nationality;

    private String experience;

    private Double currentSalary;

    private String currentProfession;

    private Boolean hasCat = false;
    private Boolean hasDog = false;
    private Boolean extraPay = false;
    private Boolean liveOut = false;
    private Boolean privateRoom = false;
    private Boolean elderlyCare = false;
    private Boolean specialNeeds = false;
    private Boolean olderThan1 = false;
    private Boolean youngerThan1 = false;

    private String currentLocation;

    @ElementCollection(targetClass = Languages.class)
    @CollectionTable(name = "application_languages", joinColumns = @JoinColumn(name = "application_id"))
    @Enumerated(EnumType.STRING)
    private Set<Languages> languages = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus;

    @Enumerated(EnumType.STRING)
    private JobInterest jobInterest;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Staff vettedBy;
    private LocalDateTime vettedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Staff approvedBy;
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Staff hiredBy;
    private LocalDateTime hiredAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaFile> mediaFiles = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void calculateAge() {
        if (dob != null) {
            this.age = Period.between(dob, LocalDate.now()).getYears();
        }
    }

    public enum Languages {
        ENGLISH, KISWAHILI, ARABIC
    }

    public enum EmploymentStatus {
        EMPLOYED, NOT_EMPLOYED
    }

    public enum JobInterest {
        LOCAL_JOBS, INTERNATIONAL_JOBS
    }

    public enum Status {PENDING, VETTED, APPROVED, REJECTED, HIRED}
}
