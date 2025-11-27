package com.yushan.engagement_service.client;

import com.yushan.engagement_service.config.FeignAuthConfig;
import com.yushan.engagement_service.dto.common.ApiResponse;
import com.yushan.engagement_service.dto.user.UserProfileResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${services.user.url:http://yushan-user-service:8081}", 
            configuration = FeignAuthConfig.class,
            fallback = UserServiceClient.UserServiceFallback.class)
public interface UserServiceClient {

    Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserProfileResponseDTO> getUser(@PathVariable("userId") UUID userId);

    default String getUsernameById(UUID userId) {
        try {
            ApiResponse<UserProfileResponseDTO> response = getUser(userId);
            if (response != null && response.getData() != null) {
                return response.getData().getUsername();
            }
            return "Unknown User";
        } catch (Exception e) {
            log.error("Error getting username for user {}: {}", userId, e.getMessage());
            return "Unknown User";
        }
    }


    /**
     * Fallback class for UserServiceClient.
     * This class will be instantiated if the user-service is down or responses with an error.
     */
    @Component
    class UserServiceFallback implements UserServiceClient {
        private static final Logger logger = LoggerFactory.getLogger(UserServiceFallback.class);

        @Override
        public ApiResponse<UserProfileResponseDTO> getUser(UUID userId) {
            logger.error("Circuit breaker opened for user-service. Falling back for getUser request with {} id.", userId);
            return ApiResponse.error(503, "User service temporarily unavailable", null);
        }
    }
}
