package com.sneakerstore.controller;

import com.sneakerstore.dto.request.SepayWebhookRequest;
import com.sneakerstore.service.SepayWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Endpoints tiếp nhận Webhook từ cổng thanh toán SePay, VietQR")
public class WebhookController {

    private final SepayWebhookService sepayWebhookService;

    @PostMapping("/sepay")
    @Operation(summary = "Tiếp nhận webhook biến động số dư từ SePay để gạch nợ tự động")
    public ResponseEntity<Map<String, Object>> handleSepayWebhook(
            @RequestBody SepayWebhookRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("[WebhookController] Nhận webhook từ SePay: gateway={}, transferType={}, amount={}, content={}",
                request.getGateway(), request.getTransferType(), request.getTransferAmount(), request.getContent());
        Map<String, Object> response = sepayWebhookService.processSepayWebhook(request, authHeader);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sepay")
    @Operation(summary = "Kiểm tra tình trạng hoạt động của Webhook SePay (Healthcheck / Ping)")
    public ResponseEntity<Map<String, Object>> pingSepayWebhook() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "service", "SneakerHub SePay Auto-Reconciliation Webhook Gateway",
                "status", "READY",
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "instruction", "Cấu hình URL webhook trên my.sepay.vn là: POST https://<domain>/api/webhooks/sepay"
        ));
    }

    @PostMapping("/sepay/simulate")
    @Operation(summary = "Mô phỏng webhook SePay để test gạch nợ tự động nhanh trên môi trường Dev")
    public ResponseEntity<Map<String, Object>> simulatePayment(
            @RequestParam String orderCode,
            @RequestParam(required = false, defaultValue = "1000000") BigDecimal amount,
            @RequestParam(required = false, defaultValue = "MBBank") String gateway) {
        SepayWebhookRequest mockRequest = SepayWebhookRequest.builder()
                .id(System.currentTimeMillis())
                .gateway(gateway)
                .transactionDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .accountNumber("0901000001")
                .content(orderCode + " chuyen tien thanh toan giay sneaker")
                .transferType("in")
                .transferAmount(amount)
                .referenceCode("SIM_" + System.currentTimeMillis())
                .description("Mô phỏng GD " + orderCode + " qua SePay")
                .build();
        return ResponseEntity.ok(sepayWebhookService.processSepayWebhook(mockRequest, null));
    }
}
