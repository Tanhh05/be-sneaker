package com.sneakerstore.service;

import com.sneakerstore.dto.request.SepayWebhookRequest;

import java.util.Map;

public interface SepayWebhookService {

    /**
     * Xử lý webhook biến động số dư từ SePay để gạch nợ tự động đơn hàng.
     *
     * @param request Payload từ SePay
     * @param authorizationHeader Giá trị header Authorization (nếu có cấu hình Apikey)
     * @return Kết quả xử lý trả về cho SePay
     */
    Map<String, Object> processSepayWebhook(SepayWebhookRequest request, String authorizationHeader);
}
