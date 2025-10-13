package com.starnet.SslAgency.interapplication.repository;

import com.starnet.SslAgency.interapplication.model.InterApplication;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface InterApplicationRepository extends JpaRepository<InterApplication, Long> {
    List<InterApplication> findByStatus(InterApplication.Status status, Sort createdAt);

}