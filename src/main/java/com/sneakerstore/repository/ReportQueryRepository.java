package com.sneakerstore.repository;

import com.sneakerstore.dto.response.ComprehensiveReportResponse;
import com.sneakerstore.dto.response.DashboardResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportQueryRepository {
    List<DashboardResponse.RevenuePointDto> queryRevenueSeries(
            String groupBy, LocalDateTime fromDate, LocalDateTime toDate, Long brandId, Long categoryId);

    long queryCompletedOrderCount(LocalDateTime fromDate, LocalDateTime toDate, Long brandId, Long categoryId);

    List<DashboardResponse.ProductSalesStatDto> queryProductSalesStats(
            LocalDateTime fromDate, LocalDateTime toDate, Long brandId, Long categoryId, boolean topSelling, int limit);

    List<DashboardResponse.StatusCountDto> queryOrderStatusCounts(LocalDateTime fromDate, LocalDateTime toDate);

    List<ComprehensiveReportResponse.EmployeeSalesStatDto> queryEmployeeStats(
            LocalDateTime fromDate, LocalDateTime toDate);

    List<ComprehensiveReportResponse.CustomerSalesStatDto> queryCustomerStats(
            LocalDateTime fromDate, LocalDateTime toDate);

    List<ComprehensiveReportResponse.PurchaseSupplierStatDto> queryPurchaseStats(
            LocalDateTime fromDate, LocalDateTime toDate);
}
