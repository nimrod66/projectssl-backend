package com.starnet.SslAgency.contract.repository;

import com.starnet.SslAgency.contract.model.ContractAmendment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContractAmendmentRepository extends JpaRepository<ContractAmendment, Long> {
    List<ContractAmendment> findByContractIdOrderByCreatedAtDesc(Long contractId);
}
