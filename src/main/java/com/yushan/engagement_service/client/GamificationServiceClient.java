package com.yushan.engagement_service.client;

import com.yushan.engagement_service.config.FeignAuthConfig;
import com.yushan.engagement_service.dto.common.ApiResponse;
import com.yushan.engagement_service.dto.gamification.VoteCheckResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "gamification-service", url = "${services.gamification.url:http://yushan-gamification-service:8085}", 
            configuration = FeignAuthConfig.class,
            fallback = GamificationServiceClient.GamificationServiceFallback.class)
public interface GamificationServiceClient {

    Logger log = LoggerFactory.getLogger(GamificationServiceClient.class);

    @GetMapping("/api/v1/gamification/votes/check")
    ApiResponse<VoteCheckResponseDTO> checkVoteEligibility();

    /**
     * Fallback class for GamificationServiceClient.
     * This class will be instantiated if the gamification-service is down or responses with an error.
     */
    @Component
    class GamificationServiceFallback implements GamificationServiceClient {
        private static final Logger logger = LoggerFactory.getLogger(GamificationServiceFallback.class);

        @Override
        public ApiResponse<VoteCheckResponseDTO> checkVoteEligibility() {
            logger.error("Circuit breaker opened for gamification-service. Falling back for checkVoteEligibility request.");
            // Return a default response indicating vote is not eligible when service is down
            VoteCheckResponseDTO fallbackResponse = new VoteCheckResponseDTO(false, 0.0, 0.0, "Gamification service temporarily unavailable");
            return ApiResponse.error(503, "Gamification service temporarily unavailable", fallbackResponse);
        }
    }
}
