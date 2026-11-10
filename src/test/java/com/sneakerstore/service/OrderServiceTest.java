package com.sneakerstore.service;

import com.sneakerstore.common.constant.AppConstants;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.repository.CustomerRepository;
import com.sneakerstore.repository.EmployeeRepository;
import com.sneakerstore.dto.response.SalesOrderDto;
import com.sneakerstore.dto.request.SalesOrderRequest;
import com.sneakerstore.entity.OrderStatusHistory;
import com.sneakerstore.entity.SalesOrder;
import com.sneakerstore.entity.SalesOrderDetail;
import com.sneakerstore.mapper.OrderMapper;
import com.sneakerstore.mapper.impl.OrderMapperImpl;
import com.sneakerstore.repository.OrderStatusHistoryRepository;
import com.sneakerstore.repository.SalesOrderDetailRepository;
import com.sneakerstore.repository.SalesOrderRepository;
import com.sneakerstore.service.impl.OrderServiceImpl;
import com.sneakerstore.entity.Payment;
import com.sneakerstore.repository.PaymentRepository;
import com.sneakerstore.entity.Product;
import com.sneakerstore.entity.Color;
import com.sneakerstore.entity.ProductVariant;
import com.sneakerstore.entity.ShoeSize;
import com.sneakerstore.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private SalesOrderDetailRepository detailRepository;
    @Mock
    private OrderStatusHistoryRepository historyRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @Spy
    private OrderMapper orderMapper = new OrderMapperImpl();

    @InjectMocks
    private OrderServiceImpl orderService;

    private ProductVariant sampleVariant;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .id(1L)
                .productCode("SP001")
                .productName("Nike Air Force 1")
                .status("ACTIVE")
                .build();
        ShoeSize size = ShoeSize.builder().id(4L).sizeValue("39").build();
        Color color = Color.builder().id(1L).colorName("Trắng").build();

        sampleVariant = ProductVariant.builder()
                .id(10L)
                .product(product)
                .size(size)
                .color(color)
                .sku("AF1-39-W")
                .importPrice(new BigDecimal("1800000"))
                .salePrice(new BigDecimal("2950000"))
                .availableQuantity(5)
                .status("ACTIVE")
                .build();
    }

    @Test
    void createOrder_ShouldDeductAvailableQuantityAndSnapshotPrice() {
        SalesOrderRequest.SalesOrderItemRequest item = new SalesOrderRequest.SalesOrderItemRequest();
        item.setProductVariantId(10L);
        item.setQuantity(2);

        SalesOrderRequest request = new SalesOrderRequest();
        request.setPaymentMethod("CASH");
        request.setStatus("COMPLETED");
        request.setItems(List.of(item));

        when(variantRepository.findByIdIn(anyCollection())).thenReturn(List.of(sampleVariant));
        when(salesOrderRepository.count()).thenReturn(0L);
        when(salesOrderRepository.existsByOrderCodeIgnoreCase(anyString())).thenReturn(false);
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.existsByPaymentCodeIgnoreCase(anyString())).thenReturn(false);

        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> {
            SalesOrder so = inv.getArgument(0);
            so.setId(100L);
            return so;
        });
        when(salesOrderRepository.findWithHeaderById(100L)).thenAnswer(inv -> Optional.of(
                SalesOrder.builder()
                        .id(100L)
                        .orderCode("HD0001")
                        .totalAmount(new BigDecimal("5900000"))
                        .finalAmount(new BigDecimal("5900000"))
                        .paymentMethod("CASH")
                        .status("COMPLETED")
                        .build()
        ));
        when(detailRepository.findBySalesOrderIdOrderByIdAsc(100L)).thenReturn(List.of());
        when(paymentRepository.findBySalesOrderIdOrderByIdAsc(100L)).thenReturn(List.of());
        when(historyRepository.findBySalesOrderIdOrderByChangedAtAscIdAsc(100L)).thenReturn(List.of());

        SalesOrderDto result = orderService.createOrder(request);

        assertNotNull(result);
        assertEquals("HD0001", result.getOrderCode());
        assertEquals(new BigDecimal("5900000"), result.getFinalAmount());
        assertEquals(3, sampleVariant.getAvailableQuantity());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(historyRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    void createOrder_WhenQuantityExceedsAvailable_ShouldThrowBusinessException() {
        SalesOrderRequest.SalesOrderItemRequest item = new SalesOrderRequest.SalesOrderItemRequest();
        item.setProductVariantId(10L);
        item.setQuantity(8); // Available is only 5

        SalesOrderRequest request = new SalesOrderRequest();
        request.setItems(List.of(item));

        when(variantRepository.findByIdIn(anyCollection())).thenReturn(List.of(sampleVariant));
        when(salesOrderRepository.count()).thenReturn(0L);

        BusinessException ex = assertThrows(BusinessException.class, () -> orderService.createOrder(request));
        assertTrue(ex.getMessage().contains("không đủ số lượng bán"));
    }

    @Test
    void updateStatus_WhenCompletedOrderCancelled_ShouldThrowBusinessException() {
        SalesOrder completedOrder = SalesOrder.builder()
                .id(50L)
                .orderCode("HD0050")
                .status(AppConstants.ORDER_COMPLETED)
                .build();
        when(salesOrderRepository.findWithHeaderById(50L)).thenReturn(Optional.of(completedOrder));

        assertThrows(BusinessException.class,
                () -> orderService.updateStatus(50L, AppConstants.ORDER_CANCELLED, "Thử hủy đơn hoàn thành"));
    }

    @Test
    void updateStatus_WhenConfirmedOrderCancelled_ShouldRestoreAvailableQuantity() {
        SalesOrder confirmedOrder = SalesOrder.builder()
                .id(60L)
                .orderCode("HD0060")
                .status(AppConstants.ORDER_CONFIRMED)
                .totalAmount(new BigDecimal("5900000"))
                .finalAmount(new BigDecimal("5900000"))
                .build();

        SalesOrderDetail detail = SalesOrderDetail.builder()
                .id(1L)
                .salesOrder(confirmedOrder)
                .productVariant(sampleVariant)
                .quantity(2)
                .unitPrice(new BigDecimal("2950000"))
                .subtotal(new BigDecimal("5900000"))
                .build();

        when(salesOrderRepository.findWithHeaderById(60L)).thenReturn(Optional.of(confirmedOrder));
        when(detailRepository.findBySalesOrderIdOrderByIdAsc(60L)).thenReturn(List.of(detail));
        when(paymentRepository.findBySalesOrderIdOrderByIdAsc(60L)).thenReturn(List.of());
        when(historyRepository.findBySalesOrderIdOrderByChangedAtAscIdAsc(60L)).thenReturn(List.of());

        SalesOrderDto dto = orderService.updateStatus(60L, AppConstants.ORDER_CANCELLED, "Khách hủy đơn");

        assertEquals(AppConstants.ORDER_CANCELLED, dto.getStatus());
        assertEquals(7, sampleVariant.getAvailableQuantity()); // 5 + 2 restored
    }
}
