package com.yushan.engagement_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vote SAGA Compensate Yuan Event
 * Published when compensation is needed (release Yuan reservation)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteSagaCompensateYuanEvent {
    
    @JsonProperty("sagaId")
    private String sagaId;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("reservationId")
    private UUID reservationId;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}


