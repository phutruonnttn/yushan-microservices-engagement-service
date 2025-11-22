package com.yushan.engagement_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Novel vote count update event for content service
 * Published when votes are created or deleted
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelVoteCountUpdateEvent {
    
    /**
     * Novel ID to update
     */
    private Integer novelId;
    
    /**
     * Total vote count (calculated from engagement-service DB)
     */
    private Integer voteCount;
    
    /**
     * Event timestamp for idempotency
     */
    private LocalDateTime timestamp;
    
    /**
     * Idempotency key: novelId-timestamp
     */
    private String idempotencyKey;
}

