package com.yushan.engagement_service.repository;

import com.yushan.engagement_service.dto.review.ReviewSearchRequestDTO;
import com.yushan.engagement_service.entity.Review;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Review aggregate.
 * Abstracts data access operations for Review entity.
 */
public interface ReviewRepository {
    
    // Basic CRUD operations
    Review findById(Integer id);
    
    Review findByUuid(UUID uuid);
    
    Review save(Review review);
    
    void delete(Integer id);
    
    // Find by foreign keys
    Review findByUserAndNovel(UUID userId, Integer novelId);
    
    List<Review> findByNovelId(Integer novelId);
    
    List<Review> findByUserId(UUID userId);
    
    // Paginated queries
    List<Review> findReviewsWithPagination(ReviewSearchRequestDTO request);
    
    // Count queries
    long countReviews(ReviewSearchRequestDTO request);
    
    // Like count update
    void updateLikeCount(Integer id, int increment);
}

