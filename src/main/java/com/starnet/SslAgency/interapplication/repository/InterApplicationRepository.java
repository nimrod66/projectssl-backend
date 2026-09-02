package com.starnet.SslAgency.interapplication.repository;

import com.starnet.SslAgency.interapplication.model.InterApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import java.util.Optional;

public interface InterApplicationRepository extends JpaRepository<InterApplication, Long> {
    List<InterApplication> findByStatus(InterApplication.Status status, Sort createdAt);

    List<InterApplication> findByNationalityAndJobRecruitment(String nationality, InterApplication.JobRecruitment jobRecruitment);

    Optional<InterApplication> findByEmail(String email);
    Optional<InterApplication> findByPhoneNumber(String phoneNumber);
}