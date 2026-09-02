package com.starnet.SslAgency.recruitment.repository;

import com.starnet.SslAgency.recruitment.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByApplicationIdOrderByOfferedAtDesc(Long applicationId);

    Optional<Offer> findByApplicationIdAndStatus(Long applicationId, Offer.Status status);
}
