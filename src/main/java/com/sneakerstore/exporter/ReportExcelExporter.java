package com.sneakerstore.exporter;

import com.sneakerstore.dto.response.ComprehensiveReportResponse;

public interface ReportExcelExporter {
    byte[] exportToExcel(ComprehensiveReportResponse report);
}
