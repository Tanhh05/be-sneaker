package com.sneakerstore.service.impl;

import com.sneakerstore.common.constant.AppConstants;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.dto.request.StoreOrderRequest;
import com.sneakerstore.dto.response.*;
import com.sneakerstore.entity.*;
import com.sneakerstore.mapper.OrderMapper;
import com.sneakerstore.repository.*;
import com.sneakerstore.service.BrandService;
import com.sneakerstore.service.CategoryService;
import com.sneakerstore.service.ProductService;
import com.sneakerstore.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final BrandService brandService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final ProductVariantRepository variantRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderDetailRepository detailRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BrandDto> getActiveBrands() {
        return brandService.getAllBrands().stream()
                .filter(b -> AppConstants.STATUS_ACTIVE.equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getActiveCategories() {
        return categoryService.getAllCategories().stream()
                .filter(c -> AppConstants.STATUS_ACTIVE.equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getStoreProducts(String keyword, Long brandId, Long categoryId, int page, int size) {
        return productService.searchProducts(
                keyword,
                brandId,
                categoryId,
                AppConstants.STATUS_ACTIVE,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getFeaturedProducts() {
        PageResponse<ProductDto> result = productService.searchProducts(
                null,
                null,
                null,
                AppConstants.STATUS_ACTIVE,
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id"))
        );
        return result.getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductDetail(Long id) {
        return productService.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StoreOrderResponse checkout(StoreOrderRequest request) {
        log.info("Khách hàng đang đặt hàng online: {}", request.getCustomerName());

        // 1. Tìm hoặc tạo mới thông tin khách hàng theo số điện thoại
        Customer customer = customerRepository.findByPhone(request.getCustomerPhone().trim())
                .orElseGet(() -> {
                    String customerCode = generateCustomerCode();
                    Customer newCustomer = Customer.builder()
                            .customerCode(customerCode)
                            .fullName(request.getCustomerName().trim())
                            .phone(request.getCustomerPhone().trim())
                            .email(request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null)
                            .address(request.getShippingAddress().trim())
                            .status(AppConstants.STATUS_ACTIVE)
                            .build();
                    return customerRepository.save(newCustomer);
                });

        // 2. Gộp sản phẩm trùng và kiểm tra tồn kho từng biến thể
        Map<Long, Integer> itemMap = new LinkedHashMap<>();
        for (StoreOrderRequest.StoreOrderItemRequest item : request.getItems()) {
            itemMap.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SalesOrderDetail> details = new ArrayList<>();
        List<ProductVariant> variantsToUpdate = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : itemMap.entrySet()) {
            Long variantId = entry.getKey();
            Integer qty = entry.getValue();

            ProductVariant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + variantId));

            if (!AppConstants.STATUS_ACTIVE.equalsIgnoreCase(variant.getStatus())) {
                throw new BusinessException("Sản phẩm '" + variant.getProduct().getProductName() + "' (SKU: " + variant.getSku() + ") hiện đã ngừng kinh doanh");
            }

            if (variant.getAvailableQuantity() < qty) {
                throw new BusinessException(String.format(
                        "Sản phẩm '%s' (SKU: %s) không đủ số lượng tồn! Hiện còn: %d đôi, bạn yêu cầu: %d đôi",
                        variant.getProduct().getProductName(), variant.getSku(), variant.getAvailableQuantity(), qty
                ));
            }

            // Trừ tồn kho khả dụng ngay để giữ hàng cho đơn online
            variant.setAvailableQuantity(variant.getAvailableQuantity() - qty);
            variantsToUpdate.add(variant);

            BigDecimal unitPrice = variant.getSalePrice() != null ? variant.getSalePrice() : BigDecimal.ZERO;
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            totalAmount = totalAmount.add(subtotal);

            SalesOrderDetail detail = SalesOrderDetail.builder()
                    .productVariant(variant)
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
            details.add(detail);
        }

        // Lưu cập nhật tồn kho
        variantRepository.saveAll(variantsToUpdate);

        // 3. Khởi tạo đơn bán hàng
        String orderCode = generateOrderCode();
        String paymentMethod = (request.getPaymentMethod() != null && request.getPaymentMethod().equalsIgnoreCase("BANK_TRANSFER"))
                ? AppConstants.PAYMENT_BANK_TRANSFER
                : AppConstants.PAYMENT_CASH;

        StringBuilder noteBuilder = new StringBuilder();
        noteBuilder.append("[ĐƠN HÀNG ONLINE] Người nhận: ").append(request.getCustomerName().trim())
                .append(" | SĐT: ").append(request.getCustomerPhone().trim())
                .append(" | Đ/C nhận: ").append(request.getShippingAddress().trim());
        if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
            noteBuilder.append(" | Ghi chú: ").append(request.getNote().trim());
        }

        SalesOrder order = SalesOrder.builder()
                .orderCode(orderCode)
                .customer(customer)
                .employee(null) // Khách đặt online, không có nhân viên tại quầy
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .finalAmount(totalAmount)
                .paymentMethod(paymentMethod)
                .status(AppConstants.ORDER_CONFIRMED)
                .note(noteBuilder.toString())
                .build();

        SalesOrder savedOrder = salesOrderRepository.save(order);

        // Lưu chi tiết đơn hàng
        for (SalesOrderDetail detail : details) {
            detail.setSalesOrder(savedOrder);
        }
        detailRepository.saveAll(details);

        // Lưu lịch sử trạng thái đơn hàng
        OrderStatusHistory history = OrderStatusHistory.builder()
                .salesOrder(savedOrder)
                .fromStatus(null)
                .toStatus(AppConstants.ORDER_CONFIRMED)
                .changedAt(LocalDateTime.now())
                .note("Khách hàng đặt hàng trực tuyến trên website Storefront")
                .build();
        historyRepository.save(history);

        // Xử lý thông tin thanh toán chuyển khoản nếu khách chọn BANK_TRANSFER
        String qrUrl = null;
        String bankInfo = null;
        if (AppConstants.PAYMENT_BANK_TRANSFER.equals(paymentMethod)) {
            String accountNo = "0901000001";
            String bankId = "MB";
            String accountName = "SNEAKERHUB PRO";
            long amount = totalAmount.longValue();
            String memo = URLEncoder.encode(orderCode, StandardCharsets.UTF_8);
            qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    bankId, accountNo, amount, memo, URLEncoder.encode(accountName, StandardCharsets.UTF_8));
            bankInfo = String.format("Ngân hàng: MBBank | STK: %s | Chủ TK: %s | Nội dung: %s", accountNo, accountName, orderCode);
        }

        List<SalesOrderDetailDto> itemDtos = details.stream()
                .map(orderMapper::toDetailDto)
                .collect(Collectors.toList());

        return StoreOrderResponse.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .orderDate(savedOrder.getOrderDate())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .shippingAddress(request.getShippingAddress())
                .totalAmount(totalAmount)
                .paymentMethod(paymentMethod)
                .status(savedOrder.getStatus())
                .qrPaymentUrl(qrUrl)
                .bankInfo(bankInfo)
                .note(savedOrder.getNote())
                .items(itemDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderDto trackOrder(String code, String phone) {
        SalesOrder order = salesOrderRepository.findByOrderCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + code));

        if (phone != null && !phone.trim().isEmpty()) {
            if (order.getCustomer() == null || !phone.trim().equals(order.getCustomer().getPhone())) {
                throw new BusinessException("Số điện thoại không khớp với thông tin đơn hàng!");
            }
        }

        SalesOrderDto dto = orderMapper.toSummaryDto(order);
        dto.setDetails(detailRepository.findBySalesOrderIdOrderByIdAsc(order.getId()).stream()
                .map(orderMapper::toDetailDto)
                .collect(Collectors.toList()));
        dto.setHistories(historyRepository.findBySalesOrderIdOrderByChangedAtAscIdAsc(order.getId()).stream()
                .map(orderMapper::toHistoryDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private String generateOrderCode() {
        long count = salesOrderRepository.count() + 1;
        String code = String.format("HD%04d", count);
        while (salesOrderRepository.existsByOrderCodeIgnoreCase(code)) {
            count++;
            code = String.format("HD%04d", count);
        }
        return code;
    }

    private String generateCustomerCode() {
        long count = customerRepository.count() + 1;
        String code = String.format("KH%04d", count);
        while (customerRepository.existsByCustomerCodeIgnoreCase(code)) {
            count++;
            code = String.format("KH%04d", count);
        }
        return code;
    }
}
