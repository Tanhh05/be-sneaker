package com.sneakerstore.service;

import com.sneakerstore.common.constant.AppConstants;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.repository.EmployeeRepository;
import com.sneakerstore.entity.Product;
import com.sneakerstore.dto.response.PurchaseOrderDto;
import com.sneakerstore.entity.PurchaseOrder;
import com.sneakerstore.entity.PurchaseOrderDetail;
import com.sneakerstore.mapper.PurchaseMapper;
import com.sneakerstore.mapper.impl.PurchaseMapperImpl;
import com.sneakerstore.repository.PurchaseOrderDetailRepository;
import com.sneakerstore.repository.PurchaseOrderRepository;
import com.sneakerstore.service.impl.PurchaseServiceImpl;
import com.sneakerstore.entity.Supplier;
import com.sneakerstore.repository.SupplierRepository;
import com.sneakerstore.entity.Color;
import com.sneakerstore.entity.ProductVariant;
import com.sneakerstore.entity.ShoeSize;
import com.sneakerstore.repository.ProductVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderDetailRepository detailRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @Spy
    private PurchaseMapper purchaseMapper = new PurchaseMapperImpl();

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @Test
    void confirm_WhenDraft_ShouldIncreaseAvailableQuantityAndSetConfirmed() {
        Supplier supplier = Supplier.builder().id(1L).supplierCode("NCC001").supplierName("NCC Đông Dương").build();
        PurchaseOrder draftPo = PurchaseOrder.builder()
                .id(10L)
                .purchaseCode("PN0010")
                .supplier(supplier)
                .totalAmount(new BigDecimal("18000000"))
                .status(AppConstants.PURCHASE_DRAFT)
                .build();

        ProductVariant variant = ProductVariant.builder()
                .id(1L)
                .product(Product.builder().id(1L).productName("Nike AF1").build())
                .size(ShoeSize.builder().id(1L).sizeValue("40").build())
                .color(Color.builder().id(1L).colorName("Trắng").build())
                .sku("AF1-40-W")
                .availableQuantity(10)
                .importPrice(new BigDecimal("1750000"))
                .salePrice(new BigDecimal("2950000"))
                .build();

        PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                .id(100L)
                .purchaseOrder(draftPo)
                .productVariant(variant)
                .quantity(10)
                .unitPrice(new BigDecimal("1800000"))
                .subtotal(new BigDecimal("18000000"))
                .build();

        when(purchaseOrderRepository.findWithHeaderById(10L)).thenReturn(Optional.of(draftPo));
        when(detailRepository.findByPurchaseOrderIdOrderByIdAsc(10L)).thenReturn(List.of(detail));

        PurchaseOrderDto dto = purchaseService.confirm(10L);

        assertEquals(AppConstants.PURCHASE_CONFIRMED, dto.getStatus());
        assertEquals(20, variant.getAvailableQuantity());
        assertEquals(new BigDecimal("1800000"), variant.getImportPrice());
        verify(variantRepository, times(1)).saveAll(anyList());
    }

    @Test
    void confirm_WhenAlreadyConfirmed_ShouldThrowBusinessException() {
        PurchaseOrder confirmedPo = PurchaseOrder.builder()
                .id(11L)
                .purchaseCode("PN0011")
                .status(AppConstants.PURCHASE_CONFIRMED)
                .build();

        when(purchaseOrderRepository.findWithHeaderById(11L)).thenReturn(Optional.of(confirmedPo));

        assertThrows(BusinessException.class, () -> purchaseService.confirm(11L));
    }
}
