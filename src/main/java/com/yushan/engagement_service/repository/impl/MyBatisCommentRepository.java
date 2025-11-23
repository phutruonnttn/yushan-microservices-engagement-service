package com.yushan.engagement_service.repository.impl;

import com.yushan.engagement_service.dao.CommentMapper;
import com.yushan.engagement_service.dto.comment.CommentSearchRequestDTO;
import com.yushan.engagement_service.entity.Comment;
import com.yushan.engagement_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * MyBatis implementation of CommentRepository.
 */
@Repository
public class MyBatisCommentRepository implements CommentRepository {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Override
    public Comment findById(Integer id) {
        return commentMapper.selectByPrimaryKey(id);
    }
    
    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            // Insert new comment
            commentMapper.insertSelective(comment);
        } else {
            // Update existing comment
            commentMapper.updateByPrimaryKeySelective(comment);
        }
        return comment;
    }
    
    @Override
    public void delete(Integer id) {
        commentMapper.deleteByPrimaryKey(id);
    }
    
    @Override
    public List<Comment> findByChapterId(Integer chapterId) {
        return commentMapper.selectByChapterId(chapterId);
    }
    
    @Override
    public List<Comment> findByUserId(UUID userId) {
        return commentMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Comment> findByNovelId(Integer novelId) {
        return commentMapper.selectByNovelId(novelId);
    }
    
    @Override
    public List<Comment> findCommentsWithPagination(CommentSearchRequestDTO searchRequest) {
        return commentMapper.selectCommentsWithPagination(searchRequest);
    }
    
    @Override
    public List<Comment> findCommentsByNovelWithPagination(
            List<Integer> chapterIds,
            Boolean isSpoiler,
            String search,
            String sort,
            String order,
            int page,
            int size
    ) {
        return commentMapper.selectCommentsByNovelWithPagination(
                chapterIds, isSpoiler, search, sort, order, page, size
        );
    }
    
    @Override
    public long countComments(CommentSearchRequestDTO searchRequest) {
        return commentMapper.countComments(searchRequest);
    }
    
    @Override
    public long countByChapterId(Integer chapterId) {
        return commentMapper.countByChapterId(chapterId);
    }
    
    @Override
    public long countByNovelId(List<Integer> chapterIds) {
        return commentMapper.countByNovelId(chapterIds);
    }
    
    @Override
    public long countCommentsByNovel(
            List<Integer> chapterIds,
            Boolean isSpoiler,
            String search
    ) {
        return commentMapper.countCommentsByNovel(chapterIds, isSpoiler, search);
    }
    
    @Override
    public void updateLikeCount(Integer id, Integer increment) {
        commentMapper.updateLikeCount(id, increment);
    }
    
    @Override
    public boolean existsByUserAndChapter(UUID userId, Integer chapterId) {
        return commentMapper.existsByUserAndChapter(userId, chapterId);
    }
    
    @Override
    public long countCommentsInLastDays(int days) {
        return commentMapper.countCommentsInLastDays(days);
    }
    
    @Override
    public long countCommentsByUser(UUID userId) {
        return commentMapper.countCommentsByUser(userId);
    }
    
    @Override
    public Comment selectMostActiveUser() {
        return commentMapper.selectMostActiveUser();
    }
    
    @Override
    public Comment selectMostCommentedChapter() {
        return commentMapper.selectMostCommentedChapter();
    }
}

