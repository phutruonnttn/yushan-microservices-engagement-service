package com.yushan.engagement_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Vote SAGA Vote Created Event
 * Published by Engagement Service after successfully creating vote
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteSagaVoteCreatedEvent {
    
    @JsonProperty("sagaId")
    private String sagaId;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("novelId")
    private Integer novelId;
    
    @JsonProperty("voteId")
    private Integer voteId;
    
    @JsonProperty("reservationId")
    private UUID reservationId;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}


