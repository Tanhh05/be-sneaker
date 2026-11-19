package com.sneakerstore.service.impl;

import com.sneakerstore.common.constant.AppConstants;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
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
import org.springframework.beans.factory.annotation.Value;
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
    private final UserRepository userRepository;

    @Value("${sepay.bank-account:0901000001}")
    private String configuredBankAccount;

    @Value("${sepay.account-name:SNEAKERHUB PRO}")
    private String configuredAccountName;

    @Value("${sepay.bank-name:MB}")
    private String configuredBankName;

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

        // Cập nhật lại họ tên, email và địa chỉ mới nhất cho khách hàng
        customer.setFullName(request.getCustomerName().trim());
        customer.setAddress(request.getShippingAddress().trim());
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()) {
            customer.setEmail(request.getCustomerEmail().trim());
        }
        customerRepository.save(customer);

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
                .status(AppConstants.ORDER_PENDING)
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
                .toStatus(AppConstants.ORDER_PENDING)
                .changedAt(LocalDateTime.now())
                .note("Khách hàng đặt hàng trực tuyến trên website Storefront (Chờ xác nhận)")
                .build();
        historyRepository.save(history);

        // Xử lý thông tin thanh toán chuyển khoản nếu khách chọn BANK_TRANSFER
        String qrUrl = null;
        String bankInfo = null;
        if (AppConstants.PAYMENT_BANK_TRANSFER.equals(paymentMethod)) {
            String accountNo = configuredBankAccount != null ? configuredBankAccount : "0901000001";
            String bankId = configuredBankName != null ? configuredBankName : "MB";
            String accountName = configuredAccountName != null ? configuredAccountName : "SNEAKERHUB PRO";
            long amount = totalAmount.longValue();
            String memo = URLEncoder.encode(orderCode, StandardCharsets.UTF_8);
            qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    bankId, accountNo, amount, memo, URLEncoder.encode(accountName, StandardCharsets.UTF_8));
            bankInfo = String.format("Ngân hàng: %s | STK: %s | Chủ TK: %s | Nội dung: %s", bankId, accountNo, accountName, orderCode);
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
        attachPaymentQrIfApplicable(dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesOrderDto> getMyOrders(String phone, String email) {
        String queryPhone = phone != null ? phone.trim() : null;
        String queryEmail = email != null ? email.trim() : null;

        // Tự động nhận diện User đang đăng nhập qua SecurityContext JWT
        String username = SecurityUtils.getCurrentUsername();
        if (!"SYSTEM".equals(username) && !"anonymousUser".equals(username)) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                if ((queryPhone == null || queryPhone.isEmpty()) && user.getPhone() != null) {
                    queryPhone = user.getPhone().trim();
                }
                if ((queryEmail == null || queryEmail.isEmpty()) && user.getEmail() != null) {
                    queryEmail = user.getEmail().trim();
                }
            }
        }

        if ((queryPhone == null || queryPhone.isEmpty()) && (queryEmail == null || queryEmail.isEmpty())) {
            return Collections.emptyList();
        }

        List<SalesOrder> orders = new ArrayList<>();
        Set<Long> seenOrderIds = new HashSet<>();

        // 1. Tìm theo Customer ID thông qua Số điện thoại hoặc Email
        if (queryPhone != null && !queryPhone.isEmpty()) {
            customerRepository.findByPhone(queryPhone).ifPresent(cust -> {
                List<SalesOrder> list = salesOrderRepository.findByCustomerIdOrderByOrderDateDesc(cust.getId());
                for (SalesOrder o : list) {
                    if (seenOrderIds.add(o.getId())) orders.add(o);
                }
            });
        }
        if (queryEmail != null && !queryEmail.isEmpty()) {
            customerRepository.findByEmail(queryEmail).ifPresent(cust -> {
                List<SalesOrder> list = salesOrderRepository.findByCustomerIdOrderByOrderDateDesc(cust.getId());
                for (SalesOrder o : list) {
                    if (seenOrderIds.add(o.getId())) orders.add(o);
                }
            });
        }

        // 2. Dự phòng thêm query trực tiếp trên SalesOrder
        List<SalesOrder> directOrders = salesOrderRepository.findOrdersByPhoneOrEmail(queryPhone, queryEmail);
        for (SalesOrder o : directOrders) {
            if (seenOrderIds.add(o.getId())) orders.add(o);
        }

        orders.sort((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()));

        return orders.stream().map(order -> {
            SalesOrderDto dto = orderMapper.toSummaryDto(order);
            dto.setDetails(detailRepository.findBySalesOrderIdOrderByIdAsc(order.getId()).stream()
                    .map(orderMapper::toDetailDto)
                    .collect(Collectors.toList()));
            dto.setHistories(historyRepository.findBySalesOrderIdOrderByChangedAtAscIdAsc(order.getId()).stream()
                    .map(orderMapper::toHistoryDto)
                    .collect(Collectors.toList()));
            dto.setPayments(paymentRepository.findBySalesOrderIdOrderByIdAsc(order.getId()).stream()
                    .map(orderMapper::toPaymentDto)
                    .collect(Collectors.toList()));
            attachPaymentQrIfApplicable(dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesOrderDto getMyOrderDetail(String orderCode, String phone) {
        SalesOrder order = salesOrderRepository.findByOrderCodeIgnoreCase(orderCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        validateOrderOwnership(order, phone);

        SalesOrderDto dto = orderMapper.toSummaryDto(order);
        dto.setDetails(detailRepository.findBySalesOrderIdOrderByIdAsc(order.getId()).stream()
                .map(orderMapper::toDetailDto)
                .collect(Collectors.toList()));
        dto.setHistories(historyRepository.findBySalesOrderIdOrderByChangedAtAscIdAsc(order.getId()).stream()
                .map(orderMapper::toHistoryDto)
                .collect(Collectors.toList()));
        dto.setPayments(paymentRepository.findBySalesOrderIdOrderByIdAsc(order.getId()).stream()
                .map(orderMapper::toPaymentDto)
                .collect(Collectors.toList()));
        attachPaymentQrIfApplicable(dto);
        return dto;
    }

    @Override
    @Transactional
    public SalesOrderDto cancelMyOrder(String orderCode, String reason, String phone) {
        SalesOrder order = salesOrderRepository.findByOrderCodeIgnoreCase(orderCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        validateOrderOwnership(order, phone);

        String currentStatus = order.getStatus();
        if (!AppConstants.ORDER_PENDING.equalsIgnoreCase(currentStatus) && !AppConstants.ORDER_DRAFT.equalsIgnoreCase(currentStatus)) {
            throw new BusinessException(String.format(
                    "Đơn hàng %s đang ở trạng thái '%s', bạn chỉ có thể tự hủy khi đơn đang ở trạng thái Chờ xác nhận (PENDING). Vui lòng liên hệ hotline hỗ trợ!",
                    orderCode, currentStatus));
        }

        // 1. Hoàn trả lại số lượng tồn kho khả dụng cho từng biến thể
        List<SalesOrderDetail> details = detailRepository.findBySalesOrderIdOrderByIdAsc(order.getId());
        List<ProductVariant> variantsToRestore = new ArrayList<>();
        for (SalesOrderDetail d : details) {
            ProductVariant pv = d.getProductVariant();
            if (pv != null) {
                int curr = pv.getAvailableQuantity() != null ? pv.getAvailableQuantity() : 0;
                pv.setAvailableQuantity(curr + d.getQuantity());
                variantsToRestore.add(pv);
            }
        }
        if (!variantsToRestore.isEmpty()) {
            variantRepository.saveAll(variantsToRestore);
        }

        // 2. Chuyển trạng thái đơn sang CANCELLED
        String actor = SecurityUtils.getCurrentUsername();
        if ("SYSTEM".equals(actor) || "anonymousUser".equals(actor)) {
            actor = "KHÁCH HÀNG (CLIENT)";
        }
        order.setStatus(AppConstants.ORDER_CANCELLED);
        order.setUpdatedBy(actor);
        SalesOrder savedOrder = salesOrderRepository.save(order);

        // 3. Ghi vết lịch sử trạng thái
        String cancelNote = (reason != null && !reason.trim().isEmpty())
                ? "Khách hàng yêu cầu hủy đơn trực tuyến: " + reason.trim()
                : "Khách hàng tự hủy đơn hàng trực tuyến trên website";
        OrderStatusHistory history = OrderStatusHistory.builder()
                .salesOrder(savedOrder)
                .fromStatus(currentStatus)
                .toStatus(AppConstants.ORDER_CANCELLED)
                .changedAt(LocalDateTime.now())
                .note(cancelNote)
                .build();
        historyRepository.save(history);

        SalesOrderDto dto = orderMapper.toSummaryDto(savedOrder);
        dto.setDetails(details.stream().map(orderMapper::toDetailDto).collect(Collectors.toList()));
        dto.setHistories(historyRepository.findBySalesOrderIdOrderByChangedAtAscIdAsc(savedOrder.getId()).stream()
                .map(orderMapper::toHistoryDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private void validateOrderOwnership(SalesOrder order, String phone) {
        String username = SecurityUtils.getCurrentUsername();
        if (!"SYSTEM".equals(username) && !"anonymousUser".equals(username)) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                Customer c = order.getCustomer();
                boolean matchesPhone = (c != null && c.getPhone() != null && user.getPhone() != null && c.getPhone().equals(user.getPhone()));
                boolean matchesEmail = (c != null && c.getEmail() != null && user.getEmail() != null && c.getEmail().equalsIgnoreCase(user.getEmail()));
                if (matchesPhone || matchesEmail) {
                    return;
                }
            }
        }
        if (phone != null && !phone.trim().isEmpty()) {
            Customer c = order.getCustomer();
            if (c != null && phone.trim().equals(c.getPhone())) {
                return;
            }
        }
        throw new BusinessException("Bạn không có quyền truy cập hoặc thao tác trên đơn hàng này!");
    }

    private void attachPaymentQrIfApplicable(SalesOrderDto dto) {
        if (dto == null) return;
        if (AppConstants.PAYMENT_BANK_TRANSFER.equalsIgnoreCase(dto.getPaymentMethod())
                && (AppConstants.ORDER_PENDING.equalsIgnoreCase(dto.getStatus()) || AppConstants.ORDER_CONFIRMED.equalsIgnoreCase(dto.getStatus()))) {
            String accountNo = configuredBankAccount != null ? configuredBankAccount : "0901000001";
            String bankId = configuredBankName != null ? configuredBankName : "MB";
            String accountName = configuredAccountName != null ? configuredAccountName : "SNEAKERHUB PRO";
            long amount = dto.getFinalAmount() != null ? dto.getFinalAmount().longValue() : 0L;
            String memo = URLEncoder.encode(dto.getOrderCode(), StandardCharsets.UTF_8);
            String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    bankId, accountNo, amount, memo, URLEncoder.encode(accountName, StandardCharsets.UTF_8));
            dto.setQrPaymentUrl(qrUrl);
            dto.setBankInfo(String.format("Ngân hàng: %s | STK: %s | Chủ TK: %s | Nội dung: %s", bankId, accountNo, accountName, dto.getOrderCode()));
        }
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
