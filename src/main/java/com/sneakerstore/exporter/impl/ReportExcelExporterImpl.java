package com.sneakerstore.exporter.impl;

import com.sneakerstore.exporter.ReportExcelExporter;

import com.sneakerstore.dto.response.ComprehensiveReportResponse;
import com.sneakerstore.dto.response.DashboardResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ReportExcelExporterImpl implements ReportExcelExporter {

    @Override
    public byte[] exportToExcel(ComprehensiveReportResponse report) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Sheet 1: Doanh thu theo thời gian
            Sheet revSheet = workbook.createSheet("BaoCaoDoanhThu");
            Row rHeader = revSheet.createRow(0);
            String[] rCols = {"Kỳ báo cáo", "Số đơn hoàn thành", "Số lượng giày đã bán", "Doanh thu (VNĐ)"};
            for (int i = 0; i < rCols.length; i++) {
                Cell cell = rHeader.createCell(i);
                cell.setCellValue(rCols[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowIdx = 1;
            for (DashboardResponse.RevenuePointDto pt : report.getRevenueSeries()) {
                Row row = revSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(pt.getPeriod());
                row.createCell(1).setCellValue(pt.getOrderCount());
                row.createCell(2).setCellValue(pt.getQuantitySold());
                row.createCell(3).setCellValue(pt.getRevenue() != null ? pt.getRevenue().doubleValue() : 0);
            }

            // Sheet 2: Sản phẩm bán chạy & bán chậm
            Sheet prodSheet = workbook.createSheet("ThongKeSanPham");
            Row pHeader = prodSheet.createRow(0);
            String[] pCols = {"Mã SP", "Tên sản phẩm", "Thương hiệu", "Danh mục", "Số lượng đã bán", "Doanh thu (VNĐ)"};
            for (int i = 0; i < pCols.length; i++) {
                Cell cell = pHeader.createCell(i);
                cell.setCellValue(pCols[i]);
                cell.setCellStyle(headerStyle);
            }
            int pIdx = 1;
            for (DashboardResponse.ProductSalesStatDto p : report.getTopSellingProducts()) {
                Row row = prodSheet.createRow(pIdx++);
                row.createCell(0).setCellValue(p.getProductCode());
                row.createCell(1).setCellValue(p.getProductName());
                row.createCell(2).setCellValue(p.getBrandName());
                row.createCell(3).setCellValue(p.getCategoryName());
                row.createCell(4).setCellValue(p.getQuantitySold());
                row.createCell(5).setCellValue(p.getRevenue() != null ? p.getRevenue().doubleValue() : 0);
            }

            // Sheet 3: Doanh thu theo nhân viên
            Sheet empSheet = workbook.createSheet("DoanhThuNhanVien");
            Row eHeader = empSheet.createRow(0);
            String[] eCols = {"Mã NV", "Họ tên nhân viên", "Số đơn hoàn thành", "Doanh thu (VNĐ)"};
            for (int i = 0; i < eCols.length; i++) {
                Cell cell = eHeader.createCell(i);
                cell.setCellValue(eCols[i]);
                cell.setCellStyle(headerStyle);
            }
            int eIdx = 1;
            for (ComprehensiveReportResponse.EmployeeSalesStatDto emp : report.getEmployeeRevenueStats()) {
                Row row = empSheet.createRow(eIdx++);
                row.createCell(0).setCellValue(emp.getEmployeeCode());
                row.createCell(1).setCellValue(emp.getEmployeeName());
                row.createCell(2).setCellValue(emp.getCompletedOrderCount());
                row.createCell(3).setCellValue(emp.getTotalRevenue() != null ? emp.getTotalRevenue().doubleValue() : 0);
            }

            // Sheet 4: Thống kê nhập hàng
            Sheet purSheet = workbook.createSheet("ThongKeNhapHang");
            Row poHeader = purSheet.createRow(0);
            String[] poCols = {"Mã NCC", "Tên nhà cung cấp", "Số phiếu đã xác nhận", "Tổng đôi nhập", "Tổng giá trị nhập (VNĐ)"};
            for (int i = 0; i < poCols.length; i++) {
                Cell cell = poHeader.createCell(i);
                cell.setCellValue(poCols[i]);
                cell.setCellStyle(headerStyle);
            }
            int poIdx = 1;
            for (ComprehensiveReportResponse.PurchaseSupplierStatDto s : report.getPurchaseStats()) {
                Row row = purSheet.createRow(poIdx++);
                row.createCell(0).setCellValue(s.getSupplierCode());
                row.createCell(1).setCellValue(s.getSupplierName());
                row.createCell(2).setCellValue(s.getConfirmedCount());
                row.createCell(3).setCellValue(s.getTotalItemsImported());
                row.createCell(4).setCellValue(s.getTotalAmount() != null ? s.getTotalAmount().doubleValue() : 0);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Lỗi xuất file Excel: " + ex.getMessage(), ex);
        }
    }
}

