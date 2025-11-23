package com.yushan.engagement_service.repository;

import com.yushan.engagement_service.dto.comment.CommentSearchRequestDTO;
import com.yushan.engagement_service.entity.Comment;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Comment aggregate.
 * Abstracts data access operations for Comment entity.
 */
public interface CommentRepository {
    
    // Basic CRUD operations
    Comment findById(Integer id);
    
    Comment save(Comment comment);
    
    void delete(Integer id);
    
    // Find by foreign keys
    List<Comment> findByChapterId(Integer chapterId);
    
    List<Comment> findByUserId(UUID userId);
    
    List<Comment> findByNovelId(Integer novelId);
    
    // Paginated queries
    List<Comment> findCommentsWithPagination(CommentSearchRequestDTO searchRequest);
    
    List<Comment> findCommentsByNovelWithPagination(
            List<Integer> chapterIds,
            Boolean isSpoiler,
            String search,
            String sort,
            String order,
            int page,
            int size
    );
    
    // Count queries
    long countComments(CommentSearchRequestDTO searchRequest);
    
    long countByChapterId(Integer chapterId);
    
    long countByNovelId(List<Integer> chapterIds);
    
    long countCommentsByNovel(
            List<Integer> chapterIds,
            Boolean isSpoiler,
            String search
    );
    
    // Like count update
    void updateLikeCount(Integer id, Integer increment);
    
    // Validation/Check queries
    boolean existsByUserAndChapter(UUID userId, Integer chapterId);
    
    // Moderation queries
    long countCommentsInLastDays(int days);
    
    long countCommentsByUser(UUID userId);
    
    Comment selectMostActiveUser();
    
    Comment selectMostCommentedChapter();
}

