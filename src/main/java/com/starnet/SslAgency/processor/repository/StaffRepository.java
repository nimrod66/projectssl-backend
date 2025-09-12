package com.starnet.SslAgency.processor.repository;

import com.starnet.SslAgency.processor.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);

    boolean existsByEmail(String email);
}
