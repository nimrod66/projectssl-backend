package com.starnet.SslAgency.employer.repository;

import com.starnet.SslAgency.employer.model.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    List<Employer> findByStatus(Employer.Status status);
}
