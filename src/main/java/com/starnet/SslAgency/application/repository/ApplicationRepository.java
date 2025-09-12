package com.starnet.SslAgency.application.repository;

import com.starnet.SslAgency.application.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @Query("SELECT a FROM Application a WHERE " +
            "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Application> search(String keyword);

    Page<Application> findByStatus(Application.Status status, Pageable pageable);

    List<Application> findByStatus(Application.Status status, Sort sort);

    List<Application> findByStatus(Application.Status status);


}
