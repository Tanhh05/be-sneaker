package com.sneakerstore.service;

import com.sneakerstore.dto.response.ComprehensiveReportResponse;
import com.sneakerstore.dto.response.DashboardResponse;

import java.time.LocalDate;

public interface ReportService {

    DashboardResponse getDashboardData();

    ComprehensiveReportResponse getComprehensiveReport(
            String groupBy, LocalDate fromDate, LocalDate toDate, Long brandId, Long categoryId);

    byte[] exportReportToExcel(
            String groupBy, LocalDate fromDate, LocalDate toDate, Long brandId, Long categoryId);
}
