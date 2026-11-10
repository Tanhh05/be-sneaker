package com.sneakerstore.controller;

import com.sneakerstore.common.response.ApiResponse;
import com.sneakerstore.dto.response.ComprehensiveReportResponse;
import com.sneakerstore.dto.response.DashboardResponse;
import com.sneakerstore.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> getDashboard() {
        return ApiResponse.ok(reportService.getDashboardData());
    }

    @GetMapping({"/summary", "/sales"})
    public ApiResponse<ComprehensiveReportResponse> getComprehensiveReport(
            @RequestParam(defaultValue = "DAY") String groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(reportService.getComprehensiveReport(groupBy, fromDate, toDate, brandId, categoryId));
    }

    @GetMapping("/revenue")
    public ApiResponse<List<DashboardResponse.RevenuePointDto>> getRevenueReport(
            @RequestParam(defaultValue = "DAY") String groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(reportService
                .getComprehensiveReport(groupBy, fromDate, toDate, brandId, categoryId)
                .getRevenueSeries());
    }

    @GetMapping("/top-products")
    public ApiResponse<List<DashboardResponse.ProductSalesStatDto>> getTopProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(reportService
                .getComprehensiveReport("DAY", fromDate, toDate, brandId, categoryId)
                .getTopSellingProducts());
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(defaultValue = "DAY") String groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId) {
        byte[] content = reportService.exportReportToExcel(groupBy, fromDate, toDate, brandId, categoryId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=BaoCao_SneakerStore.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }
}

