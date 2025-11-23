package com.yushan.engagement_service.repository.impl;

import com.yushan.engagement_service.dao.VoteMapper;
import com.yushan.engagement_service.entity.Vote;
import com.yushan.engagement_service.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * MyBatis implementation of VoteRepository.
 */
@Repository
public class MyBatisVoteRepository implements VoteRepository {
    
    @Autowired
    private VoteMapper voteMapper;
    
    @Override
    public Vote findById(Integer id) {
        return voteMapper.selectByPrimaryKey(id);
    }
    
    @Override
    public Vote save(Vote vote) {
        if (vote.getId() == null) {
            // Insert new vote
            voteMapper.insertSelective(vote);
        } else {
            // Update existing vote
            voteMapper.updateByPrimaryKeySelective(vote);
        }
        return vote;
    }
    
    @Override
    public void delete(Integer id) {
        voteMapper.deleteByPrimaryKey(id);
    }
    
    @Override
    public Vote findByUserAndNovel(UUID userId, Integer novelId) {
        return voteMapper.selectByUserAndNovel(userId, novelId);
    }
    
    @Override
    public void deleteByUserAndNovel(UUID userId, Integer novelId) {
        voteMapper.deleteByUserAndNovel(userId, novelId);
    }
    
    @Override
    public long countByUserId(UUID userId) {
        return voteMapper.countByUserId(userId);
    }
    
    @Override
    public long countByNovelId(Integer novelId) {
        return voteMapper.countByNovelId(novelId);
    }
    
    @Override
    public List<Vote> findByUserIdWithPagination(UUID userId, int offset, int limit) {
        return voteMapper.selectByUserIdWithPagination(userId, offset, limit);
    }
}

