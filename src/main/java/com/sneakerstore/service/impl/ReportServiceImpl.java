package com.sneakerstore.service.impl;

import com.sneakerstore.common.constant.AppConstants;
import com.sneakerstore.repository.CustomerRepository;
import com.sneakerstore.repository.EmployeeRepository;
import com.sneakerstore.repository.SalesOrderRepository;
import com.sneakerstore.repository.ProductRepository;
import com.sneakerstore.dto.response.ComprehensiveReportResponse;
import com.sneakerstore.dto.response.DashboardResponse;
import com.sneakerstore.exporter.ReportExcelExporter;
import com.sneakerstore.repository.ReportQueryRepository;
import com.sneakerstore.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ReportQueryRepository reportQueryRepository;
    private final ReportExcelExporter reportExcelExporter;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        // Bước 1-2: Khởi tạo mốc thời gian ngày hôm nay & đầu tháng trong Transaction ReadOnly
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByStatus(AppConstants.STATUS_ACTIVE);
        long totalCustomers = customerRepository.count();
        long totalEmployees = employeeRepository.count();
        long totalOrders = salesOrderRepository.count();
        long pendingOrders = salesOrderRepository.countByStatusIn(
                List.of(AppConstants.ORDER_DRAFT, AppConstants.ORDER_CONFIRMED));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();

        // Bước 3: Tổng hợp chỉ số doanh thu & chuỗi thời gian qua ReportQueryRepository
        BigDecimal todayRevenue = salesOrderRepository.sumCompletedRevenue(startOfToday, endOfToday);
        BigDecimal monthRevenue = salesOrderRepository.sumCompletedRevenue(startOfMonth, endOfToday);
        BigDecimal totalRevenue = salesOrderRepository.sumCompletedRevenue(null, null);

        List<DashboardResponse.ProductSalesStatDto> topProducts = reportQueryRepository.queryProductSalesStats(
                null, null, null, null, true, 5);
        List<DashboardResponse.RevenuePointDto> revByDay = reportQueryRepository.queryRevenueSeries(
                "DAY", null, null, null, null);
        List<DashboardResponse.RevenuePointDto> revByMonth = reportQueryRepository.queryRevenueSeries(
                "MONTH", null, null, null, null);
        List<DashboardResponse.RevenuePointDto> revByYear = reportQueryRepository.queryRevenueSeries(
                "YEAR", null, null, null, null);
        List<DashboardResponse.StatusCountDto> statusCounts = reportQueryRepository.queryOrderStatusCounts(null, null);

        // Bước 5: Output Mapping sang DashboardResponse DTO
        return DashboardResponse.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .totalCustomers(totalCustomers)
                .totalEmployees(totalEmployees)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
                .monthRevenue(monthRevenue != null ? monthRevenue : BigDecimal.ZERO)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .topSellingProducts(topProducts)
                .revenueByDay(revByDay)
                .revenueByMonth(revByMonth)
                .revenueByYear(revByYear)
                .orderStatusCounts(statusCounts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ComprehensiveReportResponse getComprehensiveReport(
            String groupBy, LocalDate fromDate, LocalDate toDate, Long brandId, Long categoryId) {
        // Bước 1: Chuẩn hóa tham số bộ lọc
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

        // Bước 2-3: Truy vấn thống kê đa chiều qua ReportQueryRepository
        List<DashboardResponse.RevenuePointDto> revenueSeries = reportQueryRepository.queryRevenueSeries(
                groupBy != null ? groupBy : "DAY", start, end, brandId, categoryId);

        BigDecimal totalRevenue = revenueSeries.stream()
                .map(DashboardResponse.RevenuePointDto::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalQuantitySold = revenueSeries.stream()
                .mapToLong(DashboardResponse.RevenuePointDto::getQuantitySold)
                .sum();

        long totalCompletedOrders = reportQueryRepository.queryCompletedOrderCount(start, end, brandId, categoryId);

        List<DashboardResponse.ProductSalesStatDto> topSelling = reportQueryRepository.queryProductSalesStats(
                start, end, brandId, categoryId, true, 10);
        List<DashboardResponse.ProductSalesStatDto> slowSelling = reportQueryRepository.queryProductSalesStats(
                start, end, brandId, categoryId, false, 10);

        List<DashboardResponse.StatusCountDto> orderStatusStats = reportQueryRepository.queryOrderStatusCounts(start, end);
        List<ComprehensiveReportResponse.EmployeeSalesStatDto> employeeStats = reportQueryRepository.queryEmployeeStats(start, end);
        List<ComprehensiveReportResponse.CustomerSalesStatDto> customerStats = reportQueryRepository.queryCustomerStats(start, end);
        List<ComprehensiveReportResponse.PurchaseSupplierStatDto> purchaseStats = reportQueryRepository.queryPurchaseStats(start, end);

        BigDecimal totalPurchaseAmount = purchaseStats.stream()
                .map(ComprehensiveReportResponse.PurchaseSupplierStatDto::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalConfirmedPurchases = purchaseStats.stream()
                .mapToLong(ComprehensiveReportResponse.PurchaseSupplierStatDto::getConfirmedCount)
                .sum();

        // Bước 5: Output Mapping sang ComprehensiveReportResponse DTO
        return ComprehensiveReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalCompletedOrders(totalCompletedOrders)
                .totalQuantitySold(totalQuantitySold)
                .totalPurchaseAmount(totalPurchaseAmount)
                .totalConfirmedPurchases(totalConfirmedPurchases)
                .revenueSeries(revenueSeries)
                .topSellingProducts(topSelling)
                .slowSellingProducts(slowSelling)
                .orderStatusStats(orderStatusStats)
                .employeeRevenueStats(employeeStats)
                .customerStats(customerStats)
                .purchaseStats(purchaseStats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportReportToExcel(
            String groupBy, LocalDate fromDate, LocalDate toDate, Long brandId, Long categoryId) {
        ComprehensiveReportResponse report = getComprehensiveReport(groupBy, fromDate, toDate, brandId, categoryId);
        return reportExcelExporter.exportToExcel(report);
    }
}

