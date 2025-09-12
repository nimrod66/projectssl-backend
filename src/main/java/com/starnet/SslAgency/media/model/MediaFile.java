package com.starnet.SslAgency.media.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.starnet.SslAgency.application.model.Application;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "media_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;
    @Column(nullable = false)
    private String fileUrl;
    @Column(nullable = false)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    @JsonIgnore
    private Application application;

    public enum Kind {PASSPORT, NATIONAL_ID, FULL_PHOTO, RESUME, BIRTH_CERTIFICATE, GOOD_CONDUCT, VIDEO, SHOWCASE_PHOTO}
}
