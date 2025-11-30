package com.yushan.engagement_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vote SAGA Failed Event
 * Published when SAGA fails at any step
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteSagaFailedEvent {
    
    @JsonProperty("sagaId")
    private String sagaId;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("novelId")
    private Integer novelId;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("reservationId")
    private UUID reservationId;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}


