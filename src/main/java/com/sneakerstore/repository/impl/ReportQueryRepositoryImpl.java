package com.sneakerstore.repository.impl;

import com.sneakerstore.repository.ReportQueryRepository;

import com.sneakerstore.dto.response.ComprehensiveReportResponse;
import com.sneakerstore.dto.response.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepositoryImpl implements ReportQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<DashboardResponse.RevenuePointDto> queryRevenueSeries(
            String groupBy, LocalDateTime fromDate, LocalDateTime toDate, Long brandId, Long categoryId) {
        String formatPattern = switch (groupBy != null ? groupBy.toUpperCase() : "DAY") {
            case "YEAR" -> "YYYY";
            case "MONTH" -> "YYYY-MM";
            default -> "YYYY-MM-DD";
        };

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TO_CHAR(so.ORDER_DATE, '").append(formatPattern).append("') AS period, ")
           .append("COUNT(DISTINCT so.ID) AS order_count, ")
           .append("COALESCE(SUM(sod.QUANTITY), 0) AS qty_sold, ")
           .append("COALESCE(SUM(sod.SUBTOTAL), 0) AS revenue ")
           .append("FROM SALES_ORDERS so ")
           .append("JOIN SALES_ORDER_DETAILS sod ON sod.SALES_ORDER_ID = so.ID ")
           .append("JOIN PRODUCT_VARIANTS pv ON pv.ID = sod.PRODUCT_VARIANT_ID ")
           .append("JOIN PRODUCTS p ON p.ID = pv.PRODUCT_ID ")
           .append("WHERE so.STATUS = 'COMPLETED' ");

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (fromDate != null) {
            sql.append("AND so.ORDER_DATE >= :fromDate ");
            params.addValue("fromDate", Timestamp.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append("AND so.ORDER_DATE <= :toDate ");
            params.addValue("toDate", Timestamp.valueOf(toDate));
        }
        if (brandId != null) {
            sql.append("AND p.BRAND_ID = :brandId ");
            params.addValue("brandId", brandId);
        }
        if (categoryId != null) {
            sql.append("AND p.CATEGORY_ID = :categoryId ");
            params.addValue("categoryId", categoryId);
        }

        sql.append("GROUP BY TO_CHAR(so.ORDER_DATE, '").append(formatPattern).append("') ")
           .append("ORDER BY period ASC");

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> DashboardResponse.RevenuePointDto.builder()
                .period(rs.getString("period"))
                .orderCount(rs.getLong("order_count"))
                .quantitySold(rs.getLong("qty_sold"))
                .revenue(rs.getBigDecimal("revenue"))
                .build());
    }

    @Override
    public long queryCompletedOrderCount(LocalDateTime fromDate, LocalDateTime toDate, Long brandId, Long categoryId) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(DISTINCT so.ID)
                FROM SALES_ORDERS so
                JOIN SALES_ORDER_DETAILS sod ON sod.SALES_ORDER_ID = so.ID
                JOIN PRODUCT_VARIANTS pv ON pv.ID = sod.PRODUCT_VARIANT_ID
                JOIN PRODUCTS p ON p.ID = pv.PRODUCT_ID
                WHERE so.STATUS = 'COMPLETED'
                """);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (fromDate != null) {
            sql.append(" AND so.ORDER_DATE >= :fromDate ");
            params.addValue("fromDate", Timestamp.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append(" AND so.ORDER_DATE <= :toDate ");
            params.addValue("toDate", Timestamp.valueOf(toDate));
        }
        if (brandId != null) {
            sql.append(" AND p.BRAND_ID = :brandId ");
            params.addValue("brandId", brandId);
        }
        if (categoryId != null) {
            sql.append(" AND p.CATEGORY_ID = :categoryId ");
            params.addValue("categoryId", categoryId);
        }
        Long val = jdbcTemplate.queryForObject(sql.toString(), params, Long.class);
        return val != null ? val : 0L;
    }

    @Override
    public List<DashboardResponse.ProductSalesStatDto> queryProductSalesStats(
            LocalDateTime fromDate, LocalDateTime toDate, Long brandId, Long categoryId, boolean topSelling, int limit) {
        String orderClause = topSelling
                ? "ORDER BY qty_sold DESC, revenue DESC, p.ID ASC"
                : "ORDER BY qty_sold ASC, revenue ASC, p.ID ASC";

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("limit", limit);
        StringBuilder dateJoinCond = new StringBuilder("so.ID = sod.SALES_ORDER_ID AND so.STATUS = 'COMPLETED' ");
        if (fromDate != null) {
            dateJoinCond.append("AND so.ORDER_DATE >= :fromDate ");
            params.addValue("fromDate", Timestamp.valueOf(fromDate));
        }
        if (toDate != null) {
            dateJoinCond.append("AND so.ORDER_DATE <= :toDate ");
            params.addValue("toDate", Timestamp.valueOf(toDate));
        }

        StringBuilder whereCond = new StringBuilder("WHERE 1=1 ");
        if (brandId != null) {
            whereCond.append("AND p.BRAND_ID = :brandId ");
            params.addValue("brandId", brandId);
        }
        if (categoryId != null) {
            whereCond.append("AND p.CATEGORY_ID = :categoryId ");
            params.addValue("categoryId", categoryId);
        }

        String sql = "SELECT * FROM ( " +
                "SELECT p.ID AS product_id, " +
                "       p.PRODUCT_CODE AS product_code, " +
                "       p.PRODUCT_NAME AS product_name, " +
                "       b.BRAND_NAME AS brand_name, " +
                "       c.CATEGORY_NAME AS category_name, " +
                "       COALESCE(SUM(CASE WHEN so.ID IS NOT NULL THEN sod.QUANTITY ELSE 0 END), 0) AS qty_sold, " +
                "       COALESCE(SUM(CASE WHEN so.ID IS NOT NULL THEN sod.SUBTOTAL ELSE 0 END), 0) AS revenue " +
                "FROM PRODUCTS p " +
                "JOIN BRANDS b ON b.ID = p.BRAND_ID " +
                "JOIN CATEGORIES c ON c.ID = p.CATEGORY_ID " +
                "LEFT JOIN PRODUCT_VARIANTS pv ON pv.PRODUCT_ID = p.ID " +
                "LEFT JOIN SALES_ORDER_DETAILS sod ON sod.PRODUCT_VARIANT_ID = pv.ID " +
                "LEFT JOIN SALES_ORDERS so ON " + dateJoinCond +
                whereCond +
                "GROUP BY p.ID, p.PRODUCT_CODE, p.PRODUCT_NAME, b.BRAND_NAME, c.CATEGORY_NAME " +
                orderClause + " ) WHERE ROWNUM <= :limit";

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> DashboardResponse.ProductSalesStatDto.builder()
                .productId(rs.getLong("product_id"))
                .productCode(rs.getString("product_code"))
                .productName(rs.getString("product_name"))
                .brandName(rs.getString("brand_name"))
                .categoryName(rs.getString("category_name"))
                .quantitySold(rs.getLong("qty_sold"))
                .revenue(rs.getBigDecimal("revenue"))
                .build());
    }

    @Override
    public List<DashboardResponse.StatusCountDto> queryOrderStatusCounts(LocalDateTime fromDate, LocalDateTime toDate) {
        StringBuilder sql = new StringBuilder("""
                SELECT so.STATUS AS status,
                       COUNT(*) AS cnt,
                       COALESCE(SUM(so.FINAL_AMOUNT), 0) AS total_amount
                FROM SALES_ORDERS so
                WHERE 1=1
                """);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (fromDate != null) {
            sql.append(" AND so.ORDER_DATE >= :fromDate ");
            params.addValue("fromDate", Timestamp.valueOf(fromDate));
        }
        if (toDate != null) {
            sql.append(" AND so.ORDER_DATE <= :toDate ");
            params.addValue("toDate", Timestamp.valueOf(toDate));
        }
        sql.append(" GROUP BY so.STATUS ORDER BY cnt DESC");

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> DashboardResponse.StatusCountDto.builder()
                .status(rs.getString("status"))
                .count(rs.getLong("cnt"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .build());
    }

    @Override
    public List<ComprehensiveReportResponse.EmployeeSalesStatDto> queryEmployeeStats(
            LocalDateTime fromDate, LocalDateTime toDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder joinCond = new StringBuilder("so.EMPLOYEE_ID = e.ID AND so.STATUS = 'COMPLETED' ");
        if (fromDate != null) {
            joinCond.append("AND so.ORDER_DATE >= :fromDate ");
            params.addValue("fromDate", Timestamp.valueOf(fromDate));
        }
        if (toDate != null) {
            joinCond.append("AND so.ORDER_DATE <= :toDate ");
            params.addValue("toDate", Timestamp.valueOf(toDate));
        }

        String sql = "SELECT e.ID AS employee_id, " +
                "e.EMPLOYEE_CODE AS employee_code, " +
                "e.FULL_NAME AS employee_name, " +
                "COUNT(so.ID) AS completed_cnt, " +
                "COALESCE(SUM(so.FINAL_AMOUNT), 0) AS total_rev " +
                "FROM EMPLOYEES e " +
                "LEFT JOIN SALES_ORDERS so ON " + joinCond +
                "GROUP BY e.ID, e.EMPLOYEE_CODE, e.FULL_NAME " +
                "ORDER BY total_rev DESC, e.ID ASC";

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> ComprehensiveReportResponse.EmployeeSalesStatDto.builder()
                .employeeId(rs.getLong("employee_id"))
                .employeeCode(rs.getString("employee_code"))
                .employeeName(rs.getString("employee_name"))
                .completedOrderCount(rs.getLong("completed_cnt"))
                .totalRevenue(rs.getBigDecimal("total_rev"))
                .build());
    }

    @Override
    public List<ComprehensiveReportResponse.CustomerSalesStatDto> queryCustomerStats(
            LocalDateTime fromDate, LocalDateTime toDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder joinCond = new StringBuilder("so.CUSTOMER_ID = c.ID AND so.STATUS = 'COMPLETED' ");
        if (fromDate != null) {
            joinCond.append("AND so.ORDER_DATE >= :fromDate ");
            params.addValue("fromDate", Timestamp.valueOf(fromDate));
        }
        if (toDate != null) {
            joinCond.append("AND so.ORDER_DATE <= :toDate ");
            params.addValue("toDate", Timestamp.valueOf(toDate));
        }

        String sql = "SELECT c.ID AS customer_id, " +
                "c.CUSTOMER_CODE AS customer_code, " +
                "c.FULL_NAME AS customer_name, " +
                "c.PHONE AS phone, " +
                "COUNT(so.ID) AS completed_cnt, " +
                "COALESCE(SUM(so.FINAL_AMOUNT), 0) AS total_spent " +
                "FROM CUSTOMERS c " +
                "LEFT JOIN SALES_ORDERS so ON " + joinCond +
                "GROUP BY c.ID, c.CUSTOMER_CODE, c.FULL_NAME, c.PHONE " +
                "ORDER BY total_spent DESC, c.ID ASC";

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> ComprehensiveReportResponse.CustomerSalesStatDto.builder()
                .customerId(rs.getLong("customer_id"))
                .customerCode(rs.getString("customer_code"))
                .customerName(rs.getString("customer_name"))
                .phone(rs.getString("phone"))
                .completedOrderCount(rs.getLong("completed_cnt"))
                .totalSpent(rs.getBigDecimal("total_spent"))
                .build());
    }

    @Override
    public List<ComprehensiveReportResponse.PurchaseSupplierStatDto> queryPurchaseStats(
            LocalDateTime fromDate, LocalDateTime toDate) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder joinCond = new StringBuilder("po.SUPPLIER_ID = s.ID AND po.STATUS = 'CONFIRMED' ");
        if (fromDate != null) {
            joinCond.append("AND po.PURCHASE_DATE >= :fromDate ");
            params.addValue("fromDate", Timestamp.valueOf(fromDate));
        }
        if (toDate != null) {
            joinCond.append("AND po.PURCHASE_DATE <= :toDate ");
            params.addValue("toDate", Timestamp.valueOf(toDate));
        }

        String sql = "SELECT s.ID AS supplier_id, " +
                "s.SUPPLIER_CODE AS supplier_code, " +
                "s.SUPPLIER_NAME AS supplier_name, " +
                "COUNT(DISTINCT po.ID) AS confirmed_cnt, " +
                "COALESCE(SUM(pod.QUANTITY), 0) AS items_imported, " +
                "COALESCE(SUM(pod.SUBTOTAL), 0) AS total_amount " +
                "FROM SUPPLIERS s " +
                "LEFT JOIN PURCHASE_ORDERS po ON " + joinCond +
                "LEFT JOIN PURCHASE_ORDER_DETAILS pod ON pod.PURCHASE_ORDER_ID = po.ID " +
                "GROUP BY s.ID, s.SUPPLIER_CODE, s.SUPPLIER_NAME " +
                "ORDER BY total_amount DESC, s.ID ASC";

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> ComprehensiveReportResponse.PurchaseSupplierStatDto.builder()
                .supplierId(rs.getLong("supplier_id"))
                .supplierCode(rs.getString("supplier_code"))
                .supplierName(rs.getString("supplier_name"))
                .confirmedCount(rs.getLong("confirmed_cnt"))
                .totalItemsImported(rs.getLong("items_imported"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .build());
    }
}

