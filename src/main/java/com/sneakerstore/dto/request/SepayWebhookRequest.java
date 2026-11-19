package com.sneakerstore.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO đại diện cho payload biến động số dư do SePay Webhook gửi sang.
 * Tài liệu SePay: https://sepay.vn/docs/webhook
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookRequest {

    /** ID giao dịch trên hệ thống SePay */
    private Long id;

    /** Tên ngân hàng nhận (VD: MBBank, Vietcombank, TPBank...) */
    private String gateway;

    /** Thời gian giao dịch (VD: 2026-11-19 14:30:00) */
    private String transactionDate;

    /** Số tài khoản ngân hàng nhận tiền */
    private String accountNumber;

    /** Mã tài khoản phụ (nếu có) */
    private String subAccount;

    /** Mã code đối soát do SePay phân tích */
    private String code;

    /** Nội dung chuyển khoản ngân hàng */
    private String content;

    /** Loại giao dịch: "in" (tiền vào) hoặc "out" (tiền ra) */
    private String transferType;

    /** Số tiền chuyển khoản (VNĐ) */
    private BigDecimal transferAmount;

    /** Số dư lũy kế sau giao dịch */
    private BigDecimal accumulated;

    /** Mã tham chiếu / mã chuẩn chi ngân hàng (Reference Code) */
    private String referenceCode;

    /** Toàn bộ mô tả giao dịch từ tin nhắn biến động số dư */
    private String description;
}
