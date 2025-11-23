package com.yushan.engagement_service.repository.impl;

import com.yushan.engagement_service.dao.ReviewMapper;
import com.yushan.engagement_service.dto.review.ReviewSearchRequestDTO;
import com.yushan.engagement_service.entity.Review;
import com.yushan.engagement_service.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * MyBatis implementation of ReviewRepository.
 */
@Repository
public class MyBatisReviewRepository implements ReviewRepository {
    
    @Autowired
    private ReviewMapper reviewMapper;
    
    @Override
    public Review findById(Integer id) {
        return reviewMapper.selectByPrimaryKey(id);
    }
    
    @Override
    public Review findByUuid(UUID uuid) {
        return reviewMapper.selectByUuid(uuid);
    }
    
    @Override
    public Review save(Review review) {
        if (review.getId() == null) {
            // Insert new review
            reviewMapper.insertSelective(review);
        } else {
            // Update existing review
            reviewMapper.updateByPrimaryKeySelective(review);
        }
        return review;
    }
    
    @Override
    public void delete(Integer id) {
        reviewMapper.deleteByPrimaryKey(id);
    }
    
    @Override
    public Review findByUserAndNovel(UUID userId, Integer novelId) {
        return reviewMapper.selectByUserAndNovel(userId, novelId);
    }
    
    @Override
    public List<Review> findByNovelId(Integer novelId) {
        return reviewMapper.selectByNovelId(novelId);
    }
    
    @Override
    public List<Review> findByUserId(UUID userId) {
        return reviewMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Review> findReviewsWithPagination(ReviewSearchRequestDTO request) {
        return reviewMapper.selectReviewsWithPagination(request);
    }
    
    @Override
    public long countReviews(ReviewSearchRequestDTO request) {
        return reviewMapper.countReviews(request);
    }
    
    @Override
    public void updateLikeCount(Integer id, int increment) {
        reviewMapper.updateLikeCount(id, increment);
    }
}

