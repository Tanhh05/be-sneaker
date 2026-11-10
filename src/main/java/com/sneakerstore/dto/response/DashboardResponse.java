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
public class DashboardResponse {
    private long totalProducts;
    private long activeProducts;
    private long totalCustomers;
    private long totalEmployees;
    private long totalOrders;
    private long pendingOrders;
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal totalRevenue;
    private List<ProductSalesStatDto> topSellingProducts;
    private List<RevenuePointDto> revenueByDay;
    private List<RevenuePointDto> revenueByMonth;
    private List<RevenuePointDto> revenueByYear;
    private List<StatusCountDto> orderStatusCounts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesStatDto {
        private Long productId;
        private String productCode;
        private String productName;
        private String brandName;
        private String categoryName;
        private long quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenuePointDto {
        private String period;
        private long orderCount;
        private long quantitySold;
        private BigDecimal revenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusCountDto {
        private String status;
        private long count;
        private BigDecimal totalAmount;
    }
}

