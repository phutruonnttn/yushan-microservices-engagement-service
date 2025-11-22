package com.yushan.engagement_service.entity;

import java.util.Date;
import java.util.UUID;

public class Comment {
    private Integer id;

    private UUID userId;

    private Integer chapterId;

    private String content;

    private Integer likeCnt;

    private Boolean isSpoiler;

    private Date createTime;

    private Date updateTime;

    public Comment(Integer id, UUID userId, Integer chapterId, String content, Integer likeCnt, Boolean isSpoiler, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.chapterId = chapterId;
        this.content = content;
        this.likeCnt = likeCnt;
        this.isSpoiler = isSpoiler;
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    public Comment() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getChapterId() {
        return chapterId;
    }

    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Integer getLikeCnt() {
        return likeCnt;
    }

    public void setLikeCnt(Integer likeCnt) {
        this.likeCnt = likeCnt;
    }

    public Boolean getIsSpoiler() {
        return isSpoiler;
    }

    public void setIsSpoiler(Boolean isSpoiler) {
        this.isSpoiler = isSpoiler;
    }

    public Date getCreateTime() {
        return createTime != null ? new Date(createTime.getTime()) : null;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
    }

    public Date getUpdateTime() {
        return updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    // ==================== Business Logic Methods ====================

    /**
     * Initialize as new comment with default values
     */
    public void initializeAsNew() {
        Date now = new Date();
        if (this.createTime == null) {
            this.createTime = now;
        }
        if (this.updateTime == null) {
            this.updateTime = now;
        }
        if (this.likeCnt == null) {
            this.likeCnt = 0;
        }
        if (this.isSpoiler == null) {
            this.isSpoiler = false;
        }
    }

    /**
     * Update content and update timestamp
     */
    public void updateContent(String content) {
        this.content = content != null ? content.trim() : null;
        this.updateTime = new Date();
    }

    /**
     * Increment like count
     */
    public void incrementLikeCount() {
        if (this.likeCnt == null) {
            this.likeCnt = 0;
        }
        this.likeCnt++;
        this.updateTime = new Date();
    }

    /**
     * Decrement like count (with validation)
     */
    public void decrementLikeCount() {
        if (this.likeCnt == null) {
            this.likeCnt = 0;
        }
        if (this.likeCnt > 0) {
            this.likeCnt--;
            this.updateTime = new Date();
        }
    }

    /**
     * Mark as spoiler
     */
    public void markAsSpoiler() {
        this.isSpoiler = true;
        this.updateTime = new Date();
    }

    /**
     * Mark as not spoiler
     */
    public void markAsNotSpoiler() {
        this.isSpoiler = false;
        this.updateTime = new Date();
    }

    /**
     * Set spoiler status
     */
    public void setSpoilerStatus(Boolean isSpoiler) {
        this.isSpoiler = isSpoiler != null ? isSpoiler : false;
        this.updateTime = new Date();
    }

    /**
     * Update timestamp
     */
    public void updateTimestamp() {
        this.updateTime = new Date();
    }

    // ==================== Helper Methods ====================

    /**
     * Check if comment is a spoiler
     */
    public boolean isSpoilerComment() {
        return Boolean.TRUE.equals(this.isSpoiler);
    }
}