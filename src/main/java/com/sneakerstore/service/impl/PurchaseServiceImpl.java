package com.sneakerstore.service.impl;

import com.sneakerstore.common.constant.AppConstants;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.DuplicateResourceException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.response.PageResponse;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.entity.Employee;
import com.sneakerstore.repository.EmployeeRepository;
import com.sneakerstore.dto.request.PurchaseOrderRequest;
import com.sneakerstore.dto.response.PurchaseOrderDetailDto;
import com.sneakerstore.dto.response.PurchaseOrderDto;
import com.sneakerstore.entity.PurchaseOrder;
import com.sneakerstore.entity.PurchaseOrderDetail;
import com.sneakerstore.mapper.PurchaseMapper;
import com.sneakerstore.repository.PurchaseOrderDetailRepository;
import com.sneakerstore.repository.PurchaseOrderRepository;
import com.sneakerstore.service.PurchaseService;
import com.sneakerstore.entity.Supplier;
import com.sneakerstore.repository.SupplierRepository;
import com.sneakerstore.entity.ProductVariant;
import com.sneakerstore.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository detailRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductVariantRepository variantRepository;
    private final PurchaseMapper purchaseMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderDto> searchPurchases(
            String keyword, Long supplierId, String status, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        // Bước 1: Validation & Chuẩn hóa bộ lọc ngày tháng
        LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime end = toDate != null ? toDate.atTime(LocalTime.MAX) : null;

        // Bước 2-3: Truy vấn phân trang trong Transaction ReadOnly
        Page<PurchaseOrderDto> page = purchaseOrderRepository
                .searchPurchaseOrders(keyword, supplierId, status, start, end, pageable)
                // Bước 5: Output Mapping qua PurchaseMapper
                .map(purchaseMapper::toSummaryDto);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDto getById(Long id) {
        // Bước 1: Kiểm tra tồn tại của phiếu nhập
        PurchaseOrder po = purchaseOrderRepository.findWithHeaderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập với ID: " + id));

        // Bước 5: Output Mapping (Header + Danh sách dòng chi tiết)
        PurchaseOrderDto dto = purchaseMapper.toSummaryDto(po);
        List<PurchaseOrderDetailDto> details = detailRepository.findByPurchaseOrderIdOrderByIdAsc(id)
                .stream()
                .map(purchaseMapper::toDetailDto)
                .collect(Collectors.toList());
        dto.setDetails(details);
        return dto;
    }

    @Override
    @Transactional
    public PurchaseOrderDto create(PurchaseOrderRequest request) {
        // Bước 1: Validation Logic (Nhà cung cấp, nhân viên, trùng mã phiếu nhập, biến thể sản phẩm)
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + request.getSupplierId()));

        String actor = SecurityUtils.getCurrentUsername();
        Employee employee = resolveEmployee(request.getEmployeeId(), actor);

        String code = request.getPurchaseCode();
        if (code == null || code.isBlank()) {
            long count = purchaseOrderRepository.count() + 1;
            code = String.format("PN%04d", count);
            while (purchaseOrderRepository.existsByPurchaseCodeIgnoreCase(code)) {
                count++;
                code = String.format("PN%04d", count);
            }
        } else {
            code = code.trim().toUpperCase();
            if (purchaseOrderRepository.existsByPurchaseCodeIgnoreCase(code)) {
                throw new DuplicateResourceException("Mã phiếu nhập đã tồn tại: " + code);
            }
        }

        Set<Long> variantIds = request.getItems().stream()
                .map(PurchaseOrderRequest.PurchaseItemRequest::getProductVariantId)
                .collect(Collectors.toSet());
        Map<Long, ProductVariant> variantMap = variantRepository.findByIdIn(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        // Bước 2-3: Thực thi Logic Cốt lõi (Khởi tạo phiếu nhập ở trạng thái DRAFT & tính tổng tiền)
        PurchaseOrder po = PurchaseOrder.builder()
                .purchaseCode(code)
                .supplier(supplier)
                .employee(employee)
                .purchaseDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .status(AppConstants.PURCHASE_DRAFT)
                .note(request.getNote())
                // Bước 4: Audit Log
                .createdBy(actor)
                .updatedBy(actor)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<PurchaseOrderDetail> detailEntities = new ArrayList<>();
        for (PurchaseOrderRequest.PurchaseItemRequest item : request.getItems()) {
            ProductVariant pv = variantMap.get(item.getProductVariantId());
            if (pv == null) {
                throw new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + item.getProductVariantId());
            }
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            detailEntities.add(PurchaseOrderDetail.builder()
                    .purchaseOrder(po)
                    .productVariant(pv)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(subtotal)
                    .createdBy(actor)
                    .updatedBy(actor)
                    .build());
        }

        po.setTotalAmount(total);
        po.setDetails(detailEntities);
        PurchaseOrder saved = purchaseOrderRepository.save(po);

        // Bước 5: Output Mapping (hoặc chuyển sang xác nhận ngay nếu confirmImmediately = true)
        if (request.isConfirmImmediately()) {
            return confirm(saved.getId());
        }
        return getById(saved.getId());
    }

    @Override
    @Transactional
    public PurchaseOrderDto update(Long id, PurchaseOrderRequest request) {
        // Bước 1: Validation Logic (Tồn tại phiếu nhập & State Machine: chỉ cho phép sửa khi DRAFT)
        PurchaseOrder po = purchaseOrderRepository.findWithHeaderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập với ID: " + id));

        if (!AppConstants.PURCHASE_DRAFT.equals(po.getStatus())) {
            throw new BusinessException("Chỉ được phép cập nhật phiếu nhập khi đang ở trạng thái Nháp (DRAFT)");
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp với ID: " + request.getSupplierId()));

        String actor = SecurityUtils.getCurrentUsername();
        Set<Long> variantIds = request.getItems().stream()
                .map(PurchaseOrderRequest.PurchaseItemRequest::getProductVariantId)
                .collect(Collectors.toSet());
        Map<Long, ProductVariant> variantMap = variantRepository.findByIdIn(variantIds).stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        // Bước 2-4: Thực thi Logic Cốt lõi & Audit Log
        po.setSupplier(supplier);
        po.setNote(request.getNote());
        po.setUpdatedBy(actor);
        po.getDetails().clear();

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderRequest.PurchaseItemRequest item : request.getItems()) {
            ProductVariant pv = variantMap.get(item.getProductVariantId());
            if (pv == null) {
                throw new ResourceNotFoundException("Không tìm thấy biến thể sản phẩm với ID: " + item.getProductVariantId());
            }
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            po.getDetails().add(PurchaseOrderDetail.builder()
                    .purchaseOrder(po)
                    .productVariant(pv)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(subtotal)
                    .createdBy(actor)
                    .updatedBy(actor)
                    .build());
        }

        po.setTotalAmount(total);
        purchaseOrderRepository.save(po);

        // Bước 5: Output Mapping
        return getById(id);
    }

    @Override
    @Transactional
    public PurchaseOrderDto confirm(Long id) {
        // Bước 1: Validation Logic & State Machine (Chỉ DRAFT mới được CONFIRMED)
        PurchaseOrder po = purchaseOrderRepository.findWithHeaderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập với ID: " + id));

        if (!AppConstants.PURCHASE_DRAFT.equals(po.getStatus())) {
            throw new BusinessException("Phiếu nhập đã ở trạng thái " + po.getStatus() + ", không thể xác nhận lại");
        }

        List<PurchaseOrderDetail> details = detailRepository.findByPurchaseOrderIdOrderByIdAsc(id);
        if (details.isEmpty()) {
            throw new BusinessException("Phiếu nhập không có chi tiết sản phẩm");
        }

        // Bước 2-3: Thực thi Logic Cốt lõi (Cộng tồn kho khả dụng & cập nhật giá nhập mới nhất)
        String actor = SecurityUtils.getCurrentUsername();
        List<ProductVariant> variantsToSave = new ArrayList<>();
        for (PurchaseOrderDetail d : details) {
            ProductVariant pv = d.getProductVariant();
            pv.setAvailableQuantity((pv.getAvailableQuantity() != null ? pv.getAvailableQuantity() : 0) + d.getQuantity());
            pv.setImportPrice(d.getUnitPrice());
            pv.setUpdatedBy(actor);
            variantsToSave.add(pv);
        }
        variantRepository.saveAll(variantsToSave);

        // Bước 4: Cập nhật trạng thái & Audit timestamp
        po.setStatus(AppConstants.PURCHASE_CONFIRMED);
        po.setConfirmedAt(LocalDateTime.now());
        po.setUpdatedBy(actor);
        purchaseOrderRepository.save(po);

        // Bước 5: Output Mapping
        return getById(id);
    }

    @Override
    @Transactional
    public PurchaseOrderDto cancel(Long id) {
        // Bước 1: Validation Logic & State Machine (Chỉ hủy được khi đang DRAFT)
        PurchaseOrder po = purchaseOrderRepository.findWithHeaderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập với ID: " + id));

        if (!AppConstants.PURCHASE_DRAFT.equals(po.getStatus())) {
            throw new BusinessException("Chỉ được phép hủy phiếu nhập khi đang ở trạng thái Nháp (DRAFT)");
        }

        // Bước 2-4: Chuyển trạng thái sang CANCELLED & ghi Audit Log
        po.setStatus(AppConstants.PURCHASE_CANCELLED);
        po.setCancelledAt(LocalDateTime.now());
        po.setUpdatedBy(SecurityUtils.getCurrentUsername());
        purchaseOrderRepository.save(po);

        // Bước 5: Output Mapping
        return getById(id);
    }

    private Employee resolveEmployee(Long employeeId, String username) {
        if (employeeId != null) {
            return employeeRepository.findById(employeeId).orElse(null);
        }
        return employeeRepository.findByUserUsername(username).orElse(null);
    }
}

