package com.starnet.SslAgency.media.repository;

import com.starnet.SslAgency.media.model.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
 List<MediaFile> findByApplicationId(Long applicationId);
 List<MediaFile> findByApplicationIdAndKind(Long applicationId, MediaFile.Kind kind);

}
