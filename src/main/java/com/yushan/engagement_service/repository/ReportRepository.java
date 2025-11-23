package com.yushan.engagement_service.repository;

import com.yushan.engagement_service.dto.report.ReportSearchRequestDTO;
import com.yushan.engagement_service.entity.Report;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Report aggregate.
 * Abstracts data access operations for Report entity.
 */
public interface ReportRepository {
    
    // Basic CRUD operations
    Report findById(Integer id);
    
    Report findByUuid(UUID uuid);
    
    Report save(Report report);
    
    void delete(Integer id);
    
    // Search and pagination
    List<Report> findReportsWithPagination(ReportSearchRequestDTO request);
    
    long countReports(ReportSearchRequestDTO request);
    
    // Content reports
    List<Report> findReportsByNovelId(Integer novelId);
    
    List<Report> findReportsByCommentId(Integer commentId);
    
    // User reports
    List<Report> findReportsByReporterId(UUID reporterId);
    
    // Status updates
    void updateReportStatus(Integer id, String status, String adminNotes, UUID resolvedBy);
    
    // Check if user already reported
    boolean existsReportByUserAndContent(UUID reporterId, String contentType, Integer contentId);
}

