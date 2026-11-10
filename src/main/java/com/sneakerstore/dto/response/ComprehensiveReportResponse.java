package com.sneakerstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveReportResponse {
    private BigDecimal totalRevenue;
    private long totalCompletedOrders;
    private long totalQuantitySold;
    private BigDecimal totalPurchaseAmount;
    private long totalConfirmedPurchases;

    private List<DashboardResponse.RevenuePointDto> revenueSeries;
    private List<DashboardResponse.ProductSalesStatDto> topSellingProducts;
    private List<DashboardResponse.ProductSalesStatDto> slowSellingProducts;
    private List<DashboardResponse.StatusCountDto> orderStatusStats;
    private List<EmployeeSalesStatDto> employeeRevenueStats;
    private List<CustomerSalesStatDto> customerStats;
    private List<PurchaseSupplierStatDto> purchaseStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeSalesStatDto {
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private long completedOrderCount;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerSalesStatDto {
        private Long customerId;
        private String customerCode;
        private String customerName;
        private String phone;
        private long completedOrderCount;
        private BigDecimal totalSpent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseSupplierStatDto {
        private Long supplierId;
        private String supplierCode;
        private String supplierName;
        private long confirmedCount;
        private long totalItemsImported;
        private BigDecimal totalAmount;
    }
}

