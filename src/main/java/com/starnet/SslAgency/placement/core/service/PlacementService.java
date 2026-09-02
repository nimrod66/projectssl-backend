package com.starnet.SslAgency.placement.core.service;

import com.starnet.SslAgency.applicant.model.Applicant;
import com.starnet.SslAgency.applicant.model.ApplicantTimeline;
import com.starnet.SslAgency.applicant.service.ApplicantTimelineService;
import com.starnet.SslAgency.notification.service.NotificationService;
import com.starnet.SslAgency.contract.model.Contract;
import com.starnet.SslAgency.opportunity.model.Opportunity;
import com.starnet.SslAgency.opportunity.repository.OpportunityRepository;
import com.starnet.SslAgency.placement.core.dto.PlacementChecklistDto;
import com.starnet.SslAgency.placement.core.dto.PlacementRequestDto;
import com.starnet.SslAgency.placement.core.dto.PlacementResponseDto;
import com.starnet.SslAgency.placement.core.dto.PlacementStatusHistoryDto;
import com.starnet.SslAgency.placement.core.model.Placement;
import com.starnet.SslAgency.placement.core.model.PlacementChecklist;
import com.starnet.SslAgency.placement.core.model.PlacementStatusHistory;
import com.starnet.SslAgency.placement.core.repository.CorePlacementRepository;
import com.starnet.SslAgency.placement.core.repository.CorePlacementStatusHistoryRepository;
import com.starnet.SslAgency.placement.core.repository.PlacementChecklistRepository;
import com.starnet.SslAgency.processor.model.Staff;
import com.starnet.SslAgency.recruitment.model.Application;
import com.starnet.SslAgency.recruitment.model.Offer;
import com.starnet.SslAgency.recruitment.service.ApplicationService;
import com.starnet.SslAgency.recruitment.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("corePlacementService")
public class PlacementService {

    private static final Set<Placement.Stage> ACTIVE_STAGES = Set.of(
            Placement.Stage.CREATED, Placement.Stage.DOCUMENTATION, Placement.Stage.MEDICAL,
            Placement.Stage.VISA, Placement.Stage.CONTRACT_SIGNED,
            Placement.Stage.TRAVEL_READY, Placement.Stage.DEPLOYED);

    private static final Map<Placement.Stage, Set<Placement.Stage>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(Placement.Stage.class);
        ALLOWED_TRANSITIONS.put(Placement.Stage.CREATED,
                Set.of(Placement.Stage.DOCUMENTATION, Placement.Stage.TERMINATED, Placement.Stage.RETURNED));
        ALLOWED_TRANSITIONS.put(Placement.Stage.DOCUMENTATION,
                Set.of(Placement.Stage.MEDICAL, Placement.Stage.TERMINATED, Placement.Stage.RETURNED));
        ALLOWED_TRANSITIONS.put(Placement.Stage.MEDICAL,
                Set.of(Placement.Stage.VISA, Placement.Stage.TERMINATED, Placement.Stage.RETURNED));
        ALLOWED_TRANSITIONS.put(Placement.Stage.VISA,
                Set.of(Placement.Stage.CONTRACT_SIGNED, Placement.Stage.TERMINATED, Placement.Stage.RETURNED));
        ALLOWED_TRANSITIONS.put(Placement.Stage.CONTRACT_SIGNED,
                Set.of(Placement.Stage.TRAVEL_READY, Placement.Stage.TERMINATED, Placement.Stage.RETURNED));
        ALLOWED_TRANSITIONS.put(Placement.Stage.TRAVEL_READY,
                Set.of(Placement.Stage.DEPLOYED, Placement.Stage.TERMINATED, Placement.Stage.RETURNED));
        ALLOWED_TRANSITIONS.put(Placement.Stage.DEPLOYED,
                Set.of(Placement.Stage.COMPLETED, Placement.Stage.TERMINATED, Placement.Stage.RETURNED));
    }

    @Autowired
    private CorePlacementRepository placementRepository;

    @Autowired
    private CorePlacementStatusHistoryRepository historyRepository;

    @Autowired
    private PlacementChecklistRepository checklistRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private ApplicantTimelineService timelineService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Transactional
    public PlacementResponseDto create(PlacementRequestDto dto, Staff actor) {
        Application application = applicationService.getEntity(dto.getApplicationId());
        if (application.getStatus() != Application.Status.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Placement requires an application in ACCEPTED status");
        }

        Offer acceptedOffer = offerService.getAcceptedOffer(application.getId());
        if (acceptedOffer == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No accepted offer exists for this application");
        }

        Applicant applicant = application.getApplicant();
        Opportunity opportunity = application.getOpportunity();
        Contract contract = opportunity.getContract();

        if (contract == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Placement requires an active contract on the opportunity");
        }
        if (contract.getStatus() != Contract.Status.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contract is not active");
        }

        if (opportunity.getFilledPositions() >= opportunity.getNumberOfPositions()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Opportunity has no remaining capacity");
        }

        if (placementRepository.findFirstByApplicantIdAndStageInOrderByCreatedAtDesc(
                applicant.getId(), ACTIVE_STAGES).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Applicant already has an active placement");
        }

        if (placementRepository.findByApplicationId(application.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application already has a placement");
        }

        Placement placement = Placement.builder()
                .applicant(applicant)
                .application(application)
                .acceptedOffer(acceptedOffer)
                .opportunity(opportunity)
                .employer(opportunity.getEmployer())
                .contract(contract)
                .stage(Placement.Stage.CREATED)
                .startDate(dto.getStartDate() != null ? dto.getStartDate() : acceptedOffer.getStartDate())
                .expectedEndDate(dto.getExpectedEndDate())
                .createdBy(actor)
                .build();

        placement = placementRepository.save(placement);
        placement.setPlacementNumber("PL-" + Year.now().getValue() + "-" + String.format("%05d", placement.getId()));
        placementRepository.save(placement);

        recordTransition(placement, null, Placement.Stage.CREATED, "Placement created", actor);

        seedChecklist(placement);

        opportunity.setFilledPositions(opportunity.getFilledPositions() + 1);
        opportunityRepository.save(opportunity);

        applicationService.markPlaced(application.getId());

        timelineService.log(applicant, ApplicantTimeline.EventType.PLACEMENT_CREATED,
                "Placement created for " + opportunity.getTitle(), actor,
                "placementId=" + placement.getId() + ";offerId=" + acceptedOffer.getId());

        notificationService.create(
                "Placement created: " + applicant.getFirstName() + " " + applicant.getLastName()
                        + " - " + opportunity.getTitle() + " (" + placement.getPlacementNumber() + ")",
                "PLACEMENT_ACTION", placement.getId(), "PLACEMENT",
                application.getAssignedRecruiter() != null ? application.getAssignedRecruiter().getId() : null);

        return buildResponse(placement);
    }

    @Transactional
    public PlacementResponseDto transition(Long placementId, Placement.Stage target, String reason, Staff actor) {
        Placement placement = getEntity(placementId);
        Placement.Stage current = placement.getStage();

        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid placement stage transition from " + current + " to " + target);
        }

        if (target == Placement.Stage.TERMINATED || target == Placement.Stage.RETURNED) {
            if (reason == null || reason.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A reason is mandatory");
            }
            if (target == Placement.Stage.TERMINATED) {
                placement.setTerminationReason(reason);
            } else {
                placement.setReturnReason(reason);
            }
        }

        if (target == Placement.Stage.DEPLOYED) {
            enforceDeploymentGate(placement);
        }

        placement.setStage(target);
        placementRepository.save(placement);
        recordTransition(placement, current, target, reason, actor);

        return buildResponse(placement);
    }

    @Transactional
    public PlacementChecklistDto updateCheckItem(Long placementId, PlacementChecklist.CheckItem item,
                                                 boolean completed, String notes, Staff actor) {
        Placement placement = getEntity(placementId);
        if (!placement.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Placement is " + placement.getStage() + " and cannot be updated");
        }

        PlacementChecklist check = checklistRepository
                .findByPlacementIdOrderByItemAsc(placementId).stream()
                .filter(c -> c.getItem() == item)
                .findFirst()
                .orElseGet(() -> {
                    PlacementChecklist created = PlacementChecklist.builder()
                            .placement(placement)
                            .item(item)
                            .required(true)
                            .build();
                    return checklistRepository.save(created);
                });

        check.setCompleted(completed);
        check.setCompletedAt(completed ? LocalDateTime.now() : null);
        check.setCompletedBy(completed ? actor : null);
        check.setNotes(notes);
        checklistRepository.save(check);

        return PlacementChecklistDto.from(check);
    }

    public PlacementResponseDto get(Long id) {
        return buildResponse(getEntity(id));
    }

    public List<PlacementResponseDto> listByApplicant(Long applicantId) {
        return placementRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId).stream()
                .map(this::buildResponse)
                .toList();
    }

    public List<PlacementResponseDto> listByOpportunity(Long opportunityId) {
        return placementRepository.findByOpportunityIdOrderByCreatedAtDesc(opportunityId).stream()
                .map(this::buildResponse)
                .toList();
    }

    public List<PlacementResponseDto> listActive() {
        return placementRepository.findAllByStageInOrderByCreatedAtDesc(ACTIVE_STAGES).stream()
                .map(this::buildResponse)
                .toList();
    }

    public List<PlacementResponseDto> listAll() {
        return placementRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt() == null || a.getCreatedAt() == null
                        ? 0 : b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::buildResponse)
                .toList();
    }

    public List<PlacementResponseDto> listByStage(Placement.Stage stage) {
        return placementRepository.findByStageOrderByCreatedAtDesc(stage).stream()
                .map(this::buildResponse)
                .toList();
    }

    public Placement getEntity(Long id) {
        return placementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Placement not found"));
    }

    private void seedChecklist(Placement placement) {
        for (PlacementChecklist.CheckItem item : PlacementChecklist.CheckItem.values()) {
            PlacementChecklist check = PlacementChecklist.builder()
                    .placement(placement)
                    .item(item)
                    .required(true)
                    .build();
            checklistRepository.save(check);
        }
    }

    private void enforceDeploymentGate(Placement placement) {
        List<PlacementChecklist> checks = checklistRepository.findByPlacementIdOrderByItemAsc(placement.getId());
        List<PlacementChecklist.CheckItem> missing = checks.stream()
                .filter(c -> c.isRequired() && !c.isCompleted())
                .map(PlacementChecklist::getItem)
                .toList();
        if (!missing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Deployment gate not met. Missing required checks: " + missing);
        }
    }

    private void recordTransition(Placement placement, Placement.Stage from, Placement.Stage to,
                                  String reason, Staff actor) {
        PlacementStatusHistory history = PlacementStatusHistory.builder()
                .placement(placement)
                .fromStage(from)
                .toStage(to)
                .reason(reason)
                .actor(actor)
                .build();
        historyRepository.save(history);
    }

    private PlacementResponseDto buildResponse(Placement placement) {
        List<PlacementStatusHistoryDto> history = historyRepository
                .findByPlacementIdOrderByCreatedAtAsc(placement.getId()).stream()
                .map(PlacementStatusHistoryDto::from)
                .toList();
        List<PlacementChecklistDto> checklist = checklistRepository
                .findByPlacementIdOrderByItemAsc(placement.getId()).stream()
                .map(PlacementChecklistDto::from)
                .toList();
        return PlacementResponseDto.from(placement, history, checklist);
    }
}