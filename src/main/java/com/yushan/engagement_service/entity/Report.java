package com.yushan.engagement_service.entity;

import com.yushan.engagement_service.enums.ReportStatus;

import java.util.Date;
import java.util.UUID;

public class Report {
    private Integer id;

    private UUID uuid;

    private UUID reporterId;

    private String reportType;

    private String reason;

    private String status;

    private String adminNotes;

    private UUID resolvedBy;

    private Date createdAt;

    private Date updatedAt;

    private String contentType;

    private Integer contentId;

    public Report(Integer id, UUID uuid, UUID reporterId, String reportType, String reason, String status, String adminNotes, UUID resolvedBy, Date createdAt, Date updatedAt, String contentType, Integer contentId) {
        this.id = id;
        this.uuid = uuid;
        this.reporterId = reporterId;
        this.reportType = reportType;
        this.reason = reason;
        this.status = status;
        this.adminNotes = adminNotes;
        this.resolvedBy = resolvedBy;
        this.createdAt = createdAt != null ? new Date(createdAt.getTime()) : null;
        this.updatedAt = updatedAt != null ? new Date(updatedAt.getTime()) : null;
        this.contentType = contentType;
        this.contentId = contentId;
    }

    public Report() {
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

    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType == null ? null : reportType.trim();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes == null ? null : adminNotes.trim();
    }

    public UUID getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(UUID resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public Date getCreatedAt() {
        return createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt != null ? new Date(createdAt.getTime()) : null;
    }

    public Date getUpdatedAt() {
        return updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt != null ? new Date(updatedAt.getTime()) : null;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType == null ? null : contentType.trim();
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    // ==================== Business Logic Methods ====================

    /**
     * Initialize as new report with default values
     */
    public void initializeAsNew() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        Date now = new Date();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.status == null) {
            this.status = ReportStatus.IN_REVIEW.name();
        }
    }

    /**
     * Change report status and update timestamp
     */
    public void changeStatus(ReportStatus newStatus) {
        this.status = newStatus != null ? newStatus.name() : null;
        this.updatedAt = new Date();
    }

    /**
     * Mark report as in review
     */
    public void markInReview() {
        changeStatus(ReportStatus.IN_REVIEW);
    }

    /**
     * Resolve report with admin notes and resolver UUID
     */
    public void resolve(String adminNotes, UUID resolvedBy) {
        this.status = ReportStatus.RESOLVED.name();
        this.adminNotes = adminNotes != null ? adminNotes.trim() : null;
        this.resolvedBy = resolvedBy;
        this.updatedAt = new Date();
    }

    /**
     * Dismiss report with admin notes and resolver UUID
     */
    public void dismiss(String adminNotes, UUID resolvedBy) {
        this.status = ReportStatus.DISMISSED.name();
        this.adminNotes = adminNotes != null ? adminNotes.trim() : null;
        this.resolvedBy = resolvedBy;
        this.updatedAt = new Date();
    }

    /**
     * Update admin notes and update timestamp
     */
    public void updateAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes != null ? adminNotes.trim() : null;
        this.updatedAt = new Date();
    }

    /**
     * Update timestamp
     */
    public void updateTimestamp() {
        this.updatedAt = new Date();
    }

    // ==================== Helper Methods ====================

    /**
     * Check if report is in review
     */
    public boolean isInReview() {
        return ReportStatus.IN_REVIEW.name().equals(this.status);
    }

    /**
     * Check if report is resolved
     */
    public boolean isResolved() {
        return ReportStatus.RESOLVED.name().equals(this.status);
    }

    /**
     * Check if report is dismissed
     */
    public boolean isDismissed() {
        return ReportStatus.DISMISSED.name().equals(this.status);
    }

    /**
     * Check if report can be resolved
     */
    public boolean canBeResolved() {
        return isInReview();
    }

    /**
     * Check if report can be dismissed
     */
    public boolean canBeDismissed() {
        return isInReview();
    }
}
