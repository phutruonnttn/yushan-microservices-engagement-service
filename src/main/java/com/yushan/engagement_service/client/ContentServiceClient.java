package com.yushan.engagement_service.client;

import com.yushan.engagement_service.config.FeignAuthConfig;
import com.yushan.engagement_service.dto.common.ApiResponse;
import com.yushan.engagement_service.dto.chapter.ChapterDetailResponseDTO;
import com.yushan.engagement_service.dto.novel.NovelDetailResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import feign.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@FeignClient(name = "content-service", url = "${services.content.url:http://yushan-content-service:8082}", 
            configuration = FeignAuthConfig.class,
            fallback = ContentServiceClient.ContentServiceFallback.class)
public interface ContentServiceClient {
    
    Logger log = LoggerFactory.getLogger(ContentServiceClient.class);

    @PostMapping("/api/v1/chapters/batch/get")
    ApiResponse<List<ChapterDetailResponseDTO>> getChaptersBatch(@RequestBody List<Integer> chapterIds);

    @PostMapping("/api/v1/novels/batch/get")
    ApiResponse<List<NovelDetailResponseDTO>> getNovelsBatch(@RequestBody List<Integer> novelIds);

    @GetMapping("/api/v1/novels/{novelId}")
    ApiResponse<NovelDetailResponseDTO> getNovelById(@PathVariable("novelId") Integer novelId);

    @GetMapping("/api/v1/novels/{novelId}/vote-count")
    ApiResponse<Integer> getNovelVoteCount(@PathVariable("novelId") Integer novelId);

    @PostMapping("/api/v1/novels/{novelId}/vote")
    ApiResponse<String> incrementVoteCount(@PathVariable("novelId") Integer novelId);

    @PutMapping("/api/v1/novels/{novelId}/rating")
    ApiResponse<String> updateNovelRatingAndCount(
            @PathVariable("novelId") Integer novelId,
            @RequestParam("avgRating") Float avgRating,
            @RequestParam("reviewCount") Integer reviewCount);

    @GetMapping("/api/v1/chapters/novel/{novelId}")
    ApiResponse<com.yushan.engagement_service.dto.common.PageResponseDTO<ChapterDetailResponseDTO>> getChaptersByNovelId(@PathVariable("novelId") Integer novelId);

    // Raw variant to avoid deserialization issues when only existence is needed
    @GetMapping("/api/v1/novels/{novelId}")
    ApiResponse<Map<String, Object>> getNovelByIdRaw(@PathVariable("novelId") Integer novelId);

    // Low-level variant returning only HTTP response for existence checks
    @GetMapping("/api/v1/novels/{novelId}")
    Response headlessGetNovelById(@PathVariable("novelId") Integer novelId);

    default ChapterDetailResponseDTO getChapter(Integer chapterId) {
        try {
            List<Integer> chapterIds = List.of(chapterId);
            ApiResponse<List<ChapterDetailResponseDTO>> response = getChaptersBatch(chapterIds);
            
            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                return response.getData().get(0);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    default boolean chapterExists(Integer chapterId) {
        try {
            ChapterDetailResponseDTO chapter = getChapter(chapterId);
            return chapter != null && Boolean.TRUE.equals(chapter.getIsValid());
        } catch (Exception e) {
            return false;
        }
    }

    default List<Integer> getChapterIdsByNovelId(Integer novelId) {
        try {
            ApiResponse<com.yushan.engagement_service.dto.common.PageResponseDTO<ChapterDetailResponseDTO>> response = getChaptersByNovelId(novelId);
            if (response != null && response.getData() != null && response.getData().getContent() != null) {
                return response.getData().getContent().stream()
                        .map(ChapterDetailResponseDTO::getId)
                        .collect(java.util.stream.Collectors.toList());
            }
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            log.error("Error getting chapters for novel {}: {}", novelId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }


    /**
     * Fallback class for ContentServiceClient.
     * This class will be instantiated if the content-service is down or responses with an error.
     */
    @Component
    class ContentServiceFallback implements ContentServiceClient {
        private static final Logger logger = LoggerFactory.getLogger(ContentServiceFallback.class);

        @Override
        public ApiResponse<List<ChapterDetailResponseDTO>> getChaptersBatch(List<Integer> chapterIds) {
            logger.error("Circuit breaker opened for content-service. Falling back for getChaptersBatch request with {} ids.", chapterIds.size());
            return ApiResponse.error(503, "Content service temporarily unavailable", Collections.emptyList());
        }

        @Override
        public ApiResponse<List<NovelDetailResponseDTO>> getNovelsBatch(List<Integer> novelIds) {
            logger.error("Circuit breaker opened for content-service. Falling back for getNovelsBatch request with {} ids.", novelIds.size());
            return ApiResponse.error(503, "Content service temporarily unavailable", Collections.emptyList());
        }

        @Override
        public ApiResponse<NovelDetailResponseDTO> getNovelById(Integer novelId) {
            logger.error("Circuit breaker opened for content-service. Falling back for getNovelById request with {} id.", novelId);
            return ApiResponse.error(503, "Content service temporarily unavailable", null);
        }

        @Override
        public ApiResponse<Integer> getNovelVoteCount(Integer novelId) {
            logger.error("Circuit breaker opened for content-service. Falling back for getNovelVoteCount request with {} id.", novelId);
            return ApiResponse.error(503, "Content service temporarily unavailable", 0);
        }

        @Override
        public ApiResponse<String> incrementVoteCount(Integer novelId) {
            logger.error("Circuit breaker opened for content-service. Falling back for incrementVoteCount request with {} id.", novelId);
            return ApiResponse.error(503, "Content service temporarily unavailable", "Failed to increment vote count");
        }

        @Override
        public ApiResponse<String> updateNovelRatingAndCount(Integer novelId, Float avgRating, Integer reviewCount) {
            logger.error("Circuit breaker opened for content-service. Falling back for updateNovelRatingAndCount request with {} id.", novelId);
            return ApiResponse.error(503, "Content service temporarily unavailable", "Failed to update rating");
        }

        @Override
        public ApiResponse<com.yushan.engagement_service.dto.common.PageResponseDTO<ChapterDetailResponseDTO>> getChaptersByNovelId(Integer novelId) {
            logger.error("Circuit breaker opened for content-service. Falling back for getChaptersByNovelId request with {} id.", novelId);
            return ApiResponse.error(503, "Content service temporarily unavailable", null);
        }

        @Override
        public ApiResponse<Map<String, Object>> getNovelByIdRaw(Integer novelId) {
            logger.error("Circuit breaker opened for content-service. Falling back for getNovelByIdRaw request with {} id.", novelId);
            return ApiResponse.error(503, "Content service temporarily unavailable", null);
        }

        @Override
        public Response headlessGetNovelById(Integer novelId) {
            logger.error("Circuit breaker opened for content-service. Falling back for headlessGetNovelById request with {} id.", novelId);
            // Return null for Response type fallback - caller should handle this
            return null;
        }
    }
}