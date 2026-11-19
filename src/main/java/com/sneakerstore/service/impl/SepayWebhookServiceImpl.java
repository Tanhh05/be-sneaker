package com.sneakerstore.service.impl;

import com.sneakerstore.common.constant.AppConstants;
import com.sneakerstore.dto.request.SepayWebhookRequest;
import com.sneakerstore.entity.OrderStatusHistory;
import com.sneakerstore.entity.Payment;
import com.sneakerstore.entity.SalesOrder;
import com.sneakerstore.repository.OrderStatusHistoryRepository;
import com.sneakerstore.repository.PaymentRepository;
import com.sneakerstore.repository.SalesOrderRepository;
import com.sneakerstore.service.SepayWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepayWebhookServiceImpl implements SepayWebhookService {

    private final SalesOrderRepository salesOrderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Value("${sepay.api-key:}")
    private String sepayApiKey;

    // Regex linh hoạt nhận diện mã đơn hàng dạng HD0019, DH0019, HD 0019, HD19...
    private static final Pattern ORDER_CODE_PATTERN = Pattern.compile("(?i)(?:HD|DH)\\s*(\\d{1,8})");

    @Override
    @Transactional
    public Map<String, Object> processSepayWebhook(SepayWebhookRequest request, String authorizationHeader) {
        Map<String, Object> result = new HashMap<>();

        // 1. Kiểm tra API Key nếu hệ thống có cấu hình sepay.api-key
        if (sepayApiKey != null && !sepayApiKey.trim().isEmpty()) {
            String expected = sepayApiKey.trim();
            if (authorizationHeader == null || (!authorizationHeader.equals("Apikey " + expected)
                    && !authorizationHeader.equals("Bearer " + expected)
                    && !authorizationHeader.equals(expected))) {
                log.warn("[SePay Webhook] Truy cập không hợp lệ. Authorization header: {}", authorizationHeader);
                result.put("success", false);
                result.put("message", "Unauthorized: API Key không chính xác");
                return result;
            }
        }

        if (request == null) {
            result.put("success", false);
            result.put("message", "Payload rỗng");
            return result;
        }

        // 2. Chỉ xử lý biến động số dư tiền vào (transferType == "in")
        if (request.getTransferType() != null && !"in".equalsIgnoreCase(request.getTransferType())) {
            log.info("[SePay Webhook] Bỏ qua giao dịch tiền ra (transferType: {})", request.getTransferType());
            result.put("success", true);
            result.put("message", "Đã bỏ qua giao dịch không phải tiền vào");
            return result;
        }

        // 3. Trích xuất mã đơn hàng từ nội dung giao dịch
        String orderCode = extractOrderCode(request);
        if (orderCode == null) {
            log.warn("[SePay Webhook] Không tìm thấy mã đơn hàng HDxxxx trong nội dung: content='{}', description='{}', code='{}'",
                    request.getContent(), request.getDescription(), request.getCode());
            result.put("success", false);
            result.put("message", "Không tìm thấy mã đơn hàng (HDxxxx) trong nội dung chuyển khoản");
            return result;
        }

        log.info("[SePay Webhook] Nhận diện mã đơn: '{}' từ giao dịch SePay ID #{}", orderCode, request.getId());

        // 4. Tìm kiếm đơn hàng trong cơ sở dữ liệu
        Optional<SalesOrder> orderOpt = salesOrderRepository.findByOrderCodeIgnoreCase(orderCode);
        if (orderOpt.isEmpty()) {
            log.warn("[SePay Webhook] Không tìm thấy đơn hàng '{}' trong hệ thống", orderCode);
            result.put("success", false);
            result.put("message", "Đơn hàng " + orderCode + " không tồn tại trên hệ thống");
            result.put("orderCode", orderCode);
            return result;
        }

        SalesOrder order = orderOpt.get();
        BigDecimal transferAmount = request.getTransferAmount() != null ? request.getTransferAmount() : BigDecimal.ZERO;
        String refCode = request.getReferenceCode() != null && !request.getReferenceCode().isBlank()
                ? request.getReferenceCode()
                : (request.getId() != null ? String.valueOf(request.getId()) : "REF_" + System.currentTimeMillis());

        // 5. Kiểm tra tính Idempotent (tránh xử lý trùng lặp giao dịch)
        List<Payment> existingPayments = paymentRepository.findBySalesOrderIdOrderByIdAsc(order.getId());
        boolean isDuplicate = existingPayments.stream().anyMatch(p ->
                p.getReferenceNote() != null && (
                        (request.getId() != null && p.getReferenceNote().contains("SePay ID: " + request.getId()))
                        || p.getReferenceNote().contains("Ref: " + refCode)
                )
        );

        if (isDuplicate) {
            log.info("[SePay Webhook] Giao dịch ID #{} (Ref: {}) đã được xử lý trước đó cho đơn {}",
                    request.getId(), refCode, orderCode);
            result.put("success", true);
            result.put("message", "Giao dịch đã được ghi nhận trước đó");
            result.put("orderCode", orderCode);
            result.put("status", order.getStatus());
            return result;
        }

        // 6. Tạo bản ghi Thanh toán (Payment)
        String gateway = request.getGateway() != null && !request.getGateway().isBlank() ? request.getGateway() : "MBBank";
        Payment payment = Payment.builder()
                .paymentCode(generatePaymentCode())
                .salesOrder(order)
                .paymentMethod(AppConstants.PAYMENT_BANK_TRANSFER)
                .amount(transferAmount)
                .paymentDate(LocalDateTime.now())
                .status("PAID")
                .referenceNote(String.format("SePay ID: %s | Cổng: %s | Ref: %s | Ngày GD: %s | ND: %s",
                        request.getId() != null ? request.getId() : "N/A",
                        gateway,
                        refCode,
                        request.getTransactionDate() != null ? request.getTransactionDate() : LocalDateTime.now().toString(),
                        request.getContent() != null ? request.getContent() : ""))
                .createdBy("SYSTEM_SEPAY")
                .updatedBy("SYSTEM_SEPAY")
                .build();
        paymentRepository.save(payment);

        // 7. Cập nhật trạng thái đơn hàng và lịch sử kiểm toán
        String previousStatus = order.getStatus();
        boolean isFullyPaid = order.getFinalAmount() != null && transferAmount.compareTo(order.getFinalAmount()) >= 0;

        if (AppConstants.ORDER_PENDING.equalsIgnoreCase(previousStatus) || AppConstants.ORDER_DRAFT.equalsIgnoreCase(previousStatus)) {
            if (isFullyPaid) {
                order.setStatus(AppConstants.ORDER_CONFIRMED);
                String payNote = String.format("[SePay VietQR: Đã thanh toán %,dđ]", transferAmount.longValue());
                order.setNote(order.getNote() != null && !order.getNote().isBlank()
                        ? order.getNote() + " | " + payNote
                        : payNote);
                salesOrderRepository.save(order);

                // Ghi nhận lịch sử chuyển trạng thái
                historyRepository.save(OrderStatusHistory.builder()
                        .salesOrder(order)
                        .fromStatus(previousStatus)
                        .toStatus(AppConstants.ORDER_CONFIRMED)
                        .note(String.format("SePay gạch nợ tự động: Khách chuyển %,d đ qua %s (Mã GD: %s). Hệ thống tự động xác nhận đơn.",
                                transferAmount.longValue(), gateway, refCode))
                        .changedAt(LocalDateTime.now())
                        .createdBy("SYSTEM_SEPAY")
                        .updatedBy("SYSTEM_SEPAY")
                        .build());

                log.info("[SePay Webhook] Đơn hàng '{}' đã được tự động duyệt từ {} -> CONFIRMED", orderCode, previousStatus);
            } else {
                // Khách chuyển thiếu tiền -> ghi nhận thanh toán một phần, giữ PENDING
                historyRepository.save(OrderStatusHistory.builder()
                        .salesOrder(order)
                        .fromStatus(previousStatus)
                        .toStatus(previousStatus)
                        .note(String.format("SePay ghi nhận chuyển khoản một phần: %,d đ / Tổng %,d đ qua %s (Mã GD: %s)",
                                transferAmount.longValue(),
                                order.getFinalAmount() != null ? order.getFinalAmount().longValue() : 0L,
                                gateway,
                                refCode))
                        .changedAt(LocalDateTime.now())
                        .createdBy("SYSTEM_SEPAY")
                        .updatedBy("SYSTEM_SEPAY")
                        .build());
            }
        } else {
            // Đơn đã ở trạng thái khác (ví dụ CONFIRMED/SHIPPING), vẫn ghi nhận lịch sử thanh toán
            historyRepository.save(OrderStatusHistory.builder()
                    .salesOrder(order)
                    .fromStatus(previousStatus)
                    .toStatus(previousStatus)
                    .note(String.format("Ghi nhận thêm thanh toán chuyển khoản %,d đ qua SePay (%s - Mã GD: %s)",
                            transferAmount.longValue(), gateway, refCode))
                    .changedAt(LocalDateTime.now())
                    .createdBy("SYSTEM_SEPAY")
                    .updatedBy("SYSTEM_SEPAY")
                    .build());
        }

        result.put("success", true);
        result.put("message", "Xử lý thanh toán thành công và đã tự động gạch nợ đơn hàng");
        result.put("orderCode", orderCode);
        result.put("orderStatus", order.getStatus());
        result.put("amountReceived", transferAmount);
        return result;
    }

    private String extractOrderCode(SepayWebhookRequest request) {
        // 1. Kiểm tra trong content (nội dung chuyển khoản)
        String found = matchPattern(request.getContent());
        if (found != null) return found;

        // 2. Kiểm tra trong description (toàn bộ SMS biến động số dư)
        found = matchPattern(request.getDescription());
        if (found != null) return found;

        // 3. Kiểm tra trong code (mã code SePay nhận diện)
        found = matchPattern(request.getCode());
        if (found != null) return found;

        return null;
    }

    private String matchPattern(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher m = ORDER_CODE_PATTERN.matcher(text);
        if (m.find()) {
            String digits = m.group(1);
            if (digits.length() < 4) {
                return String.format("HD%04d", Integer.parseInt(digits));
            } else {
                return "HD" + digits;
            }
        }
        return null;
    }

    private String generatePaymentCode() {
        long count = paymentRepository.count() + 1;
        String code = String.format("TT%04d", count);
        while (paymentRepository.existsByPaymentCodeIgnoreCase(code)) {
            count++;
            code = String.format("TT%04d", count);
        }
        return code;
    }
}
