package com.starnet.SslAgency.contract.repository;

import com.starnet.SslAgency.contract.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByStatus(Contract.Status status);
    List<Contract> findByEmployerId(Long employerId);
    List<Contract> findByEmployerIdAndStatus(Long employerId, Contract.Status status);
}
