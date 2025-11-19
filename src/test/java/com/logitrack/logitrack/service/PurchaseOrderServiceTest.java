package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.PurchaseOrder.CreatePurchaseOrderRequestDto;
import com.logitrack.logitrack.dto.PurchaseOrder.POLineResponseDto;
import com.logitrack.logitrack.dto.PurchaseOrder.PurchaseOrderLineRequestDto;
import com.logitrack.logitrack.dto.PurchaseOrder.PurchaseOrderResponseDto;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.POStatus;
import com.logitrack.logitrack.exception.BusinessException;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.repository.ProductRepository;
import com.logitrack.logitrack.repository.PurchaseOrderLineRepository;
import com.logitrack.logitrack.repository.PurchaseOrderRepository;
import com.logitrack.logitrack.repository.SupplierRepository;
import com.logitrack.logitrack.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private PurchaseOrderLineRepository poLineRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private Supplier mockSupplier;
    private Product mockProduct;
    private PurchaseOrder mockPO;
    private Warehouse mockWarehouse;
    private CreatePurchaseOrderRequestDto createRequest;

    @BeforeEach
    void setUp() {
        // إعداد البيانات المشتركة
        mockSupplier = Supplier.builder().id(1L).name("Test Supplier").build();
        mockProduct = Product.builder().id(1L).name("Test Product").sku("SKU-1").active(true).build();
        mockWarehouse = Warehouse.builder().id(1L).name("Main WH").build();

        mockPO = PurchaseOrder.builder()
                .id(1L)
                .supplier(mockSupplier)
                .status(POStatus.DRAFT)
                .lines(new ArrayList<>())
                .createdAt(Instant.now())
                .build();

        // إعداد Request DTO
        PurchaseOrderLineRequestDto lineRequest = new PurchaseOrderLineRequestDto();
        lineRequest.setProductId(1L);
        lineRequest.setQuantity(10L);
        lineRequest.setUnitPrice(BigDecimal.valueOf(100));

        createRequest = new CreatePurchaseOrderRequestDto();
        createRequest.setSupplierId(1L);
        createRequest.setLines(Collections.singletonList(lineRequest));
    }

    // =================================================
    // 1. Tests for createPurchaseOrder
    // =================================================

    @Test
    @DisplayName("createPurchaseOrder: Should create PO successfully when data is valid")
    void createPurchaseOrder_Success() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(mockPO);

        // Mocking ModelMapper (Use lenient because it's called inside a private method)
        lenient().when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(new PurchaseOrderResponseDto());
        lenient().when(modelMapper.map(any(PurchaseOrderLine.class), eq(POLineResponseDto.class)))
                .thenReturn(new POLineResponseDto());

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.createPurchaseOrder(createRequest);

        // Then
        assertNotNull(result);
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("createPurchaseOrder: Should throw ResourceNotFoundException when Supplier not found")
    void createPurchaseOrder_SupplierNotFound() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.createPurchaseOrder(createRequest));

        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPurchaseOrder: Should throw ResourceNotFoundException when Product not found")
    void createPurchaseOrder_ProductNotFound() {
        // Given
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.createPurchaseOrder(createRequest));
    }

    @Test
    @DisplayName("createPurchaseOrder: Should throw BusinessException when Product is inactive")
    void createPurchaseOrder_ProductInactive() {
        // Given
        mockProduct.setActive(false); // Make product inactive
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(mockSupplier));
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> purchaseOrderService.createPurchaseOrder(createRequest));

        assertTrue(ex.getMessage().contains("is inactive"));
    }

    // =================================================
    // 2. Tests for approvePurchaseOrder
    // =================================================

    @Test
    @DisplayName("approvePurchaseOrder: Should approve successfully when status is DRAFT")
    void approvePurchaseOrder_Success() {
        // Given
        mockPO.setStatus(POStatus.DRAFT);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(mockPO));
        when(purchaseOrderRepository.save(mockPO)).thenReturn(mockPO);

        lenient().when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(new PurchaseOrderResponseDto());

        // When
        purchaseOrderService.approvePurchaseOrder(1L);

        // Then
        assertEquals(POStatus.APPROVED, mockPO.getStatus());
        verify(purchaseOrderRepository).save(mockPO);
    }

    @Test
    @DisplayName("approvePurchaseOrder: Should throw exception if PO not DRAFT")
    void approvePurchaseOrder_WrongStatus() {
        // Given
        mockPO.setStatus(POStatus.APPROVED); // Already approved
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(mockPO));

        // When & Then
        assertThrows(BusinessException.class, () -> purchaseOrderService.approvePurchaseOrder(1L));
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("approvePurchaseOrder: Should throw exception if PO not found")
    void approvePurchaseOrder_NotFound() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> purchaseOrderService.approvePurchaseOrder(1L));
    }

    // =================================================
    // 3. Tests for receiveFullPurchaseOrder
    // =================================================

    @Test
    @DisplayName("receiveFullPurchaseOrder: Should receive stock and update status")
    void receiveFullPurchaseOrder_Success() {
        // Given
        mockPO.setStatus(POStatus.APPROVED);
        PurchaseOrderLine line = PurchaseOrderLine.builder()
                .product(mockProduct)
                .quantity(10L)
                .build();
        mockPO.getLines().add(line);

        // Note: Using the specific custom method name from your service
        when(purchaseOrderRepository.findByIdWithLinesAndProducts(1L)).thenReturn(Optional.of(mockPO));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mockWarehouse));

        // When
        purchaseOrderService.receiveFullPurchaseOrder(1L, 1L);

        // Then
        assertEquals(POStatus.RECEIVED, mockPO.getStatus());
        verify(inventoryService).receiveStockAndFulfillBackorders(mockProduct, mockWarehouse, 10L);
        verify(purchaseOrderRepository).save(mockPO);
    }

    @Test
    @DisplayName("receiveFullPurchaseOrder: Should skip line if quantity is 0")
    void receiveFullPurchaseOrder_SkipZeroQuantity() {
        // Given
        mockPO.setStatus(POStatus.APPROVED);
        PurchaseOrderLine line = PurchaseOrderLine.builder()
                .product(mockProduct)
                .quantity(0L) // Zero quantity
                .build();
        mockPO.getLines().add(line);

        when(purchaseOrderRepository.findByIdWithLinesAndProducts(1L)).thenReturn(Optional.of(mockPO));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mockWarehouse));

        // When
        purchaseOrderService.receiveFullPurchaseOrder(1L, 1L);

        // Then
        // Inventory service should NOT be called
        verify(inventoryService, never()).receiveStockAndFulfillBackorders(any(), any(), any());
        // Status should still update
        assertEquals(POStatus.RECEIVED, mockPO.getStatus());
    }

    @Test
    @DisplayName("receiveFullPurchaseOrder: Should throw exception if status not APPROVED")
    void receiveFullPurchaseOrder_WrongStatus() {
        // Given
        mockPO.setStatus(POStatus.DRAFT); // Not approved yet
        when(purchaseOrderRepository.findByIdWithLinesAndProducts(1L)).thenReturn(Optional.of(mockPO));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mockWarehouse));

        // When & Then
        assertThrows(BusinessException.class, () -> purchaseOrderService.receiveFullPurchaseOrder(1L, 1L));
        verify(inventoryService, never()).receiveStockAndFulfillBackorders(any(), any(), any());
    }

    @Test
    @DisplayName("receiveFullPurchaseOrder: Should throw exception if Warehouse not found")
    void receiveFullPurchaseOrder_WarehouseNotFound() {
        when(purchaseOrderRepository.findByIdWithLinesAndProducts(1L)).thenReturn(Optional.of(mockPO));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> purchaseOrderService.receiveFullPurchaseOrder(1L, 1L));
    }
}
