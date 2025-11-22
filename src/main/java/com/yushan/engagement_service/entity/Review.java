package com.yushan.engagement_service.entity;

import java.util.Date;
import java.util.UUID;

public class Review {
    private Integer id;

    private UUID uuid;

    private UUID userId;

    private Integer novelId;

    private Integer rating;

    private String title;

    private String content;

    private Integer likeCnt;

    private Boolean isSpoiler;

    private Date createTime;

    private Date updateTime;

    public Review(Integer id, UUID uuid, UUID userId, Integer novelId, Integer rating, String title, String content, Integer likeCnt, Boolean isSpoiler, Date createTime, Date updateTime) {
        this.id = id;
        this.uuid = uuid;
        this.userId = userId;
        this.novelId = novelId;
        this.rating = rating;
        this.title = title;
        this.content = content;
        this.likeCnt = likeCnt;
        this.isSpoiler = isSpoiler;
        this.createTime = createTime != null ? new Date(createTime.getTime()) : null;
        this.updateTime = updateTime != null ? new Date(updateTime.getTime()) : null;
    }

    public Review() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getNovelId() {
        return novelId;
    }

    public void setNovelId(Integer novelId) {
        this.novelId = novelId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
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
     * Initialize as new review with default values
     */
    public void initializeAsNew() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
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
     * Update title and update timestamp
     */
    public void updateTitle(String title) {
        this.title = title != null ? title.trim() : null;
        this.updateTime = new Date();
    }

    /**
     * Update rating and update timestamp
     */
    public void updateRating(Integer rating) {
        this.rating = rating;
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
     * Check if review is a spoiler
     */
    public boolean isSpoilerReview() {
        return Boolean.TRUE.equals(this.isSpoiler);
    }
}