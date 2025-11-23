package com.yushan.engagement_service.repository;

import com.yushan.engagement_service.entity.Vote;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Vote aggregate.
 * Abstracts data access operations for Vote entity.
 */
public interface VoteRepository {
    
    // Basic CRUD operations
    Vote findById(Integer id);
    
    Vote save(Vote vote);
    
    void delete(Integer id);
    
    // Find by foreign keys
    Vote findByUserAndNovel(UUID userId, Integer novelId);
    
    void deleteByUserAndNovel(UUID userId, Integer novelId);
    
    // Count queries
    long countByUserId(UUID userId);
    
    long countByNovelId(Integer novelId);
    
    // Paginated queries
    List<Vote> findByUserIdWithPagination(UUID userId, int offset, int limit);
}

