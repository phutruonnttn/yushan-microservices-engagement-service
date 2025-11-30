package com.yushan.engagement_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yushan.engagement_service.dto.event.VoteSagaYuanReservedEvent;
import com.yushan.engagement_service.dto.event.VoteSagaVoteCreatedEvent;
import com.yushan.engagement_service.dto.event.VoteSagaFailedEvent;
import com.yushan.engagement_service.dto.event.VoteSagaCompensateYuanEvent;
import com.yushan.engagement_service.dto.common.ApiResponse;
import com.yushan.engagement_service.dto.novel.NovelDetailResponseDTO;
import com.yushan.engagement_service.entity.Vote;
import com.yushan.engagement_service.exception.ValidationException;
import com.yushan.engagement_service.repository.VoteRepository;
import com.yushan.engagement_service.client.ContentServiceClient;
import com.yushan.engagement_service.service.IdempotencyService;
import com.yushan.engagement_service.service.KafkaEventProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Vote SAGA Listener for Engagement Service
 * Handles vote creation SAGA steps in Choreography pattern
 */
@Slf4j
@Component
public class VoteSagaListener {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private KafkaEventProducerService kafkaEventProducerService;

    @Autowired
    private ContentServiceClient contentServiceClient;

    private static final String SAGA_TOPIC_YUAN_RESERVED = "vote-saga.yuan-reserved";
    private static final String SAGA_TOPIC_VOTE_CREATED = "vote-saga.vote-created";
    private static final String SAGA_TOPIC_FAILED = "vote-saga.failed";
    private static final String SAGA_TOPIC_COMPENSATE = "vote-saga.compensate-yuan";

    /**
     * Step 2: Yuan Reserved - Create Vote
     * Listens to vote-saga.yuan-reserved topic
     */
    @KafkaListener(topics = SAGA_TOPIC_YUAN_RESERVED, groupId = "engagement-service-vote-saga")
    public void handleVoteSagaYuanReserved(@Payload String eventJson) {
        try {
            log.info("Received VoteSagaYuanReservedEvent: {}", eventJson);
            
            VoteSagaYuanReservedEvent event = objectMapper.readValue(eventJson, VoteSagaYuanReservedEvent.class);
            
            // Idempotency check
            String idempotencyKey = "idempotency:vote-saga-create:" + event.getSagaId();
            if (idempotencyService.isProcessed(idempotencyKey, "VoteSagaCreate")) {
                log.info("VoteSagaYuanReservedEvent already processed, skipping: sagaId={}", event.getSagaId());
                return;
            }

            // Validate novel exists (defensive validation)
            try {
                ApiResponse<NovelDetailResponseDTO> novelResp = 
                    contentServiceClient.getNovelById(event.getNovelId());
                if (novelResp == null || novelResp.getData() == null) {
                    throw new ValidationException("Novel does not exist: " + event.getNovelId());
                }
                // Author cannot vote own novel (defensive check)
                if (novelResp.getData().getAuthorId() != null && 
                    novelResp.getData().getAuthorId().equals(event.getUserId())) {
                    throw new ValidationException("Cannot vote your own novel");
                }
            } catch (ValidationException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to validate novel: {}", event.getNovelId(), e);
                throw new ValidationException("Failed to validate novel: " + e.getMessage());
            }
            
            // Create vote
            Vote vote = new Vote();
            vote.setUserId(event.getUserId());
            vote.setNovelId(event.getNovelId());
            vote.initializeAsNew();
            voteRepository.save(vote);
            
            // Calculate vote count
            Integer voteCount = (int) voteRepository.countByNovelId(event.getNovelId());
            
            // Publish vote created event
            publishVoteCreatedEvent(event, vote.getId(), voteCount);
            
            // Mark as processed
            idempotencyService.markAsProcessed(idempotencyKey, "VoteSagaCreate");
            
            log.info("Successfully created vote in SAGA: sagaId={}, voteId={}", 
                    event.getSagaId(), vote.getId());
                    
        } catch (ValidationException e) {
            log.error("Validation failed for VoteSagaYuanReservedEvent: {}", eventJson, e);
            handleSagaFailure(eventJson, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing VoteSagaYuanReservedEvent: {}", eventJson, e);
            handleSagaFailure(eventJson, "Failed to create vote: " + e.getMessage());
        }
    }

    /**
     * Handle SAGA failure - Delete vote if created and trigger compensation
     * NOTE: With balance check at reserve time, votes will not be created if balance is insufficient.
     * This listener is kept for backward compatibility but should not receive events in normal flow.
     */
    // @KafkaListener(topics = SAGA_TOPIC_FAILED, groupId = "engagement-service-vote-saga")
    @Deprecated
    public void handleVoteSagaFailed(@Payload String eventJson) {
        try {
            log.info("Received VoteSagaFailedEvent: {}", eventJson);
            
            VoteSagaFailedEvent event = objectMapper.readValue(eventJson, VoteSagaFailedEvent.class);
            
            // Check if vote was created using IdempotencyService
            String idempotencyKey = "idempotency:vote-saga-create:" + event.getSagaId();
            if (idempotencyService.isProcessed(idempotencyKey, "VoteSagaCreate")) {
                log.warn("Vote was created before SAGA failed, deleting vote as compensation: sagaId={}", event.getSagaId());
                
                // Delete vote as compensation (rollback)
                if (event.getUserId() != null && event.getNovelId() != null) {
                    try {
                        Vote vote = voteRepository.findByUserAndNovel(event.getUserId(), event.getNovelId());
                        if (vote != null) {
                            voteRepository.delete(vote.getId());
                            log.info("Successfully deleted vote as compensation: sagaId={}, voteId={}, userId={}, novelId={}", 
                                    event.getSagaId(), vote.getId(), event.getUserId(), event.getNovelId());
                            
                            // Update vote count for content service
                            Integer voteCount = (int) voteRepository.countByNovelId(event.getNovelId());
                            kafkaEventProducerService.publishNovelVoteCountUpdateEvent(event.getNovelId(), voteCount);
                        } else {
                            log.warn("Vote not found for compensation: userId={}, novelId={}, sagaId={}", 
                                    event.getUserId(), event.getNovelId(), event.getSagaId());
                        }
                    } catch (Exception e) {
                        log.error("Failed to delete vote during compensation: sagaId={}", event.getSagaId(), e);
                    }
                } else {
                    log.warn("Cannot delete vote: missing userId or novelId in VoteSagaFailedEvent: sagaId={}", 
                            event.getSagaId());
                }
            }
            
            log.info("SAGA failed, compensation handled: sagaId={}, reason={}", event.getSagaId(), event.getReason());
            
        } catch (Exception e) {
            log.error("Error processing VoteSagaFailedEvent: {}", eventJson, e);
        }
    }

    /**
     * Publish vote created event
     */
    private void publishVoteCreatedEvent(VoteSagaYuanReservedEvent yuanReservedEvent, Integer voteId, Integer voteCount) {
        try {
            VoteSagaVoteCreatedEvent event = VoteSagaVoteCreatedEvent.builder()
                    .sagaId(yuanReservedEvent.getSagaId())
                    .userId(yuanReservedEvent.getUserId())
                    .novelId(yuanReservedEvent.getNovelId())
                    .voteId(voteId)
                    .reservationId(yuanReservedEvent.getReservationId())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Publish to SAGA topic
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(SAGA_TOPIC_VOTE_CREATED, yuanReservedEvent.getSagaId(), eventJson);
            log.info("Published VoteSagaVoteCreatedEvent: sagaId={}, voteId={}", 
                    yuanReservedEvent.getSagaId(), voteId);
            
            // Also publish vote count update event (for content service)
            kafkaEventProducerService.publishNovelVoteCountUpdateEvent(yuanReservedEvent.getNovelId(), voteCount);
            
        } catch (Exception e) {
            log.error("Failed to publish VoteSagaVoteCreatedEvent: sagaId={}", yuanReservedEvent.getSagaId(), e);
            throw new RuntimeException("Failed to publish vote created event", e);
        }
    }

    /**
     * Handle SAGA failure
     */
    private void handleSagaFailure(String eventJson, String reason) {
        try {
            VoteSagaYuanReservedEvent event = objectMapper.readValue(eventJson, VoteSagaYuanReservedEvent.class);
            
            VoteSagaFailedEvent failedEvent = VoteSagaFailedEvent.builder()
                    .sagaId(event.getSagaId())
                    .userId(event.getUserId())
                    .novelId(event.getNovelId())
                    .reason(reason)
                    .reservationId(event.getReservationId())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            // Trigger compensation (release Yuan)
            VoteSagaCompensateYuanEvent compensateEvent = VoteSagaCompensateYuanEvent.builder()
                    .sagaId(event.getSagaId())
                    .userId(event.getUserId())
                    .reservationId(event.getReservationId())
                    .reason("Vote creation failed: " + reason)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            kafkaTemplate.send(SAGA_TOPIC_COMPENSATE, event.getSagaId(), compensateEvent);
            kafkaTemplate.send(SAGA_TOPIC_FAILED, event.getSagaId(), failedEvent);
            
            log.info("Published compensation events: sagaId={}, reason={}", event.getSagaId(), reason);
            
        } catch (Exception e) {
            log.error("Failed to publish SAGA failure events", e);
        }
    }

}

