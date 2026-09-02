package com.starnet.SslAgency.recruitment.service;

import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import com.starnet.SslAgency.applicant.service.ApplicantTimelineService;
import com.starnet.SslAgency.notification.service.NotificationService;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.recruitment.dto.OfferRequestDto;
import com.starnet.SslAgency.recruitment.dto.OfferResponseDto;
import com.starnet.SslAgency.recruitment.model.Application;
import com.starnet.SslAgency.recruitment.model.Offer;
import com.starnet.SslAgency.recruitment.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicantTimelineService timelineService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public OfferResponseDto create(OfferRequestDto dto, Staff actor) {
        Application application = applicationService.getEntity(dto.getApplicationId());
        if (application.getStatus() != Application.Status.INTERVIEW
                && application.getStatus() != Application.Status.OFFERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Offers can only be made for interviewed applications");
        }

        Offer offer = Offer.builder()
                .application(application)
                .offeredSalary(dto.getOfferedSalary())
                .currency(dto.getCurrency())
                .positionTitle(dto.getPositionTitle())
                .startDate(dto.getStartDate())
                .benefits(dto.getBenefits())
                .conditions(dto.getConditions())
                .status(Offer.Status.PENDING)
                .expiresAt(dto.getExpiresAt() != null ? dto.getExpiresAt().atStartOfDay()
                        : LocalDateTime.now().plusDays(7))
                .build();

        offer = offerRepository.save(offer);
        applicationService.markOffered(application.getId());

        timelineService.log(application.getApplicant(), ApplicantTimeline.EventType.OFFER_MADE,
                "Offer made for " + application.getOpportunity().getTitle()
                        + (dto.getOfferedSalary() != null ? " - " + dto.getCurrency() + " " + dto.getOfferedSalary() : ""),
                actor, "offerId=" + offer.getId());

        notificationService.create(
                "Offer pending response: " + application.getApplicant().getFirstName() + " "
                        + application.getApplicant().getLastName() + " - " + application.getOpportunity().getTitle(),
                "PENDING_APPROVAL", offer.getId(), "OFFER",
                application.getAssignedRecruiter() != null ? application.getAssignedRecruiter().getId() : null);

        return OfferResponseDto.from(offer);
    }

    @Transactional
    public OfferResponseDto accept(Long offerId, Staff actor) {
        Offer offer = getEntity(offerId);
        requireStatus(offer, Offer.Status.PENDING);

        Optional<Offer> accepted = offerRepository.findByApplicationIdAndStatus(
                offer.getApplication().getId(), Offer.Status.ACCEPTED);
        if (accepted.isPresent() && !accepted.get().getId().equals(offerId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Application already has an accepted offer");
        }

        offer.setStatus(Offer.Status.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        offerRepository.save(offer);

        Application application = offer.getApplication();
        applicationService.acceptOffer(application.getId());

        timelineService.log(application.getApplicant(), ApplicantTimeline.EventType.OFFER_ACCEPTED,
                "Offer accepted for " + application.getOpportunity().getTitle(), actor,
                "offerId=" + offer.getId());

        notificationService.create(
                "Offer accepted: " + application.getApplicant().getFirstName() + " "
                        + application.getApplicant().getLastName() + " - " + application.getOpportunity().getTitle(),
                "STATUS_CHANGE", offer.getId(), "OFFER",
                application.getAssignedRecruiter() != null ? application.getAssignedRecruiter().getId() : null);

        return OfferResponseDto.from(offer);
    }

    @Transactional
    public OfferResponseDto reject(Long offerId, String reason, Staff actor) {
        Offer offer = getEntity(offerId);
        requireStatus(offer, Offer.Status.PENDING);

        offer.setStatus(Offer.Status.REJECTED);
        offer.setRejectionReason(reason);
        offer.setRespondedAt(LocalDateTime.now());
        offerRepository.save(offer);

        Application application = offer.getApplication();
        if (application.getStatus() == Application.Status.OFFERED) {
            applicationService.reject(application.getId(),
                    Application.RejectionReason.CANDIDATE_WITHDREW, reason, actor);
        }

        timelineService.log(application.getApplicant(), ApplicantTimeline.EventType.OFFER_REJECTED,
                "Offer rejected for " + application.getOpportunity().getTitle()
                        + (reason != null ? " - " + reason : ""),
                actor, "offerId=" + offer.getId());

        return OfferResponseDto.from(offer);
    }

    @Transactional
    public OfferResponseDto withdraw(Long offerId, String reason, Staff actor) {
        Offer offer = getEntity(offerId);
        requireStatus(offer, Offer.Status.PENDING);

        offer.setStatus(Offer.Status.WITHDRAWN);
        offer.setRejectionReason(reason);
        offerRepository.save(offer);

        return OfferResponseDto.from(offer);
    }

    @Transactional
    public OfferResponseDto expire(Long offerId, Staff actor) {
        Offer offer = getEntity(offerId);
        requireStatus(offer, Offer.Status.PENDING);

        offer.setStatus(Offer.Status.EXPIRED);
        offer.setRejectionReason("Offer expired");
        offer.setRespondedAt(LocalDateTime.now());
        offerRepository.save(offer);

        return OfferResponseDto.from(offer);
    }

    public List<OfferResponseDto> listByApplication(Long applicationId) {
        return offerRepository.findByApplicationIdOrderByOfferedAtDesc(applicationId).stream()
                .map(OfferResponseDto::from)
                .toList();
    }

    public Offer getAcceptedOffer(Long applicationId) {
        return offerRepository.findByApplicationIdAndStatus(applicationId, Offer.Status.ACCEPTED)
                .orElse(null);
    }

    public Offer getEntity(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
    }

    private void requireStatus(Offer offer, Offer.Status... expected) {
        for (Offer.Status status : expected) {
            if (offer.getStatus() == status) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Invalid offer status " + offer.getStatus() + " for this operation");
    }
}