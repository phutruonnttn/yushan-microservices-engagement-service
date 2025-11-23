package com.yushan.engagement_service.repository.impl;

import com.yushan.engagement_service.dao.ReportMapper;
import com.yushan.engagement_service.dto.report.ReportSearchRequestDTO;
import com.yushan.engagement_service.entity.Report;
import com.yushan.engagement_service.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * MyBatis implementation of ReportRepository.
 */
@Repository
public class MyBatisReportRepository implements ReportRepository {
    
    @Autowired
    private ReportMapper reportMapper;
    
    @Override
    public Report findById(Integer id) {
        return reportMapper.selectByPrimaryKey(id);
    }
    
    @Override
    public Report findByUuid(UUID uuid) {
        return reportMapper.selectByUuid(uuid);
    }
    
    @Override
    public Report save(Report report) {
        if (report.getId() == null) {
            // Insert new report
            reportMapper.insertSelective(report);
        } else {
            // Update existing report
            reportMapper.updateByPrimaryKeySelective(report);
        }
        return report;
    }
    
    @Override
    public void delete(Integer id) {
        reportMapper.deleteByPrimaryKey(id);
    }
    
    @Override
    public List<Report> findReportsWithPagination(ReportSearchRequestDTO request) {
        return reportMapper.selectReportsWithPagination(request);
    }
    
    @Override
    public long countReports(ReportSearchRequestDTO request) {
        return reportMapper.countReports(request);
    }
    
    @Override
    public List<Report> findReportsByNovelId(Integer novelId) {
        return reportMapper.selectReportsByNovelId(novelId);
    }
    
    @Override
    public List<Report> findReportsByCommentId(Integer commentId) {
        return reportMapper.selectReportsByCommentId(commentId);
    }
    
    @Override
    public List<Report> findReportsByReporterId(UUID reporterId) {
        return reportMapper.selectReportsByReporterId(reporterId);
    }
    
    @Override
    public void updateReportStatus(Integer id, String status, String adminNotes, UUID resolvedBy) {
        reportMapper.updateReportStatus(id, status, adminNotes, resolvedBy);
    }
    
    @Override
    public boolean existsReportByUserAndContent(UUID reporterId, String contentType, Integer contentId) {
        return reportMapper.existsReportByUserAndContent(reporterId, contentType, contentId);
    }
}

