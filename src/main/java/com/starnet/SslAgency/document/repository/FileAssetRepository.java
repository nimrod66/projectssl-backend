package com.starnet.SslAgency.document.repository;

import com.starnet.SslAgency.document.model.FileAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {
}