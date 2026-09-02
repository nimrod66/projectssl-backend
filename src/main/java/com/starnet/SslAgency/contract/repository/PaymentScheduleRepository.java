package com.starnet.SslAgency.contract.repository;

import com.starnet.SslAgency.contract.model.PaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentScheduleRepository extends JpaRepository<PaymentSchedule, Long> {
    List<PaymentSchedule> findByContractIdOrderByDueDateAsc(Long contractId);
    long countByContractIdAndPaidFalse(Long contractId);
}
