package com.yushan.engagement_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Novel rating update event for content service
 * Published when reviews are created, updated, or deleted
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelRatingUpdateEvent {
    
    /**
     * Novel ID to update
     */
    private Integer novelId;
    
    /**
     * Calculated average rating
     */
    private Float avgRating;
    
    /**
     * Total review count
     */
    private Integer reviewCount;
    
    /**
     * Event timestamp for idempotency
     */
    private LocalDateTime timestamp;
    
    /**
     * Idempotency key: novelId-timestamp
     */
    private String idempotencyKey;
}

