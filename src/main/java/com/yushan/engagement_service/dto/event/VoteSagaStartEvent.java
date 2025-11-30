package com.yushan.engagement_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vote SAGA Start Event
 * Published by Engagement Service when starting vote creation SAGA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteSagaStartEvent {
    
    @JsonProperty("sagaId")
    private String sagaId;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("novelId")
    private Integer novelId;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}


