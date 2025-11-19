package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Shipment.ShipmentDTO;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.MovementType;
import com.logitrack.logitrack.entity.enums.SOStatus;
import com.logitrack.logitrack.entity.enums.ShipmentStatus;
import com.logitrack.logitrack.exception.BusinessException;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.exception.StockUnavailableException;
import com.logitrack.logitrack.repository.InventoryMovementRepository;
import com.logitrack.logitrack.repository.InventoryRepository;
import com.logitrack.logitrack.repository.SalesOrderRepository;
import com.logitrack.logitrack.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private SalesOrderRepository salesOrderRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    // Dummy Data Objects
    private Shipment mockShipment;
    private SalesOrder mockOrder;
    private Warehouse mockWarehouse;
    private Product mockProduct;
    private Inventory mockInventory;
    private SalesOrderLine mockLine;

    @BeforeEach
    void setUp() {
        // 1. Setup Product
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setSku("SKU-101");

        // 2. Setup Warehouse
        mockWarehouse = new Warehouse();
        mockWarehouse.setId(100L);

        // 3. Setup Inventory (Initially sufficient)
        mockInventory = new Inventory();
        mockInventory.setId(1L);
        mockInventory.setProduct(mockProduct);
        mockInventory.setWarehouse(mockWarehouse);
        mockInventory.setQuantityOnHand(50L);
        mockInventory.setQuantityReserved(10L); // We have 10 reserved

        // 4. Setup Order Line (Requesting 5 items)
        mockLine = new SalesOrderLine();
        mockLine.setProduct(mockProduct);
        mockLine.setQuantity(5L);

        // 5. Setup SalesOrder
        mockOrder = new SalesOrder();
        mockOrder.setId(500L);
        mockOrder.setWarehouse(mockWarehouse);
        mockOrder.setStatus(SOStatus.RESERVED); // Correct initial status
        mockOrder.setLines(Collections.singletonList(mockLine));

        // 6. Setup Shipment
        mockShipment = new Shipment();
        mockShipment.setId(1L);
        mockShipment.setSalesOrder(mockOrder);
        mockShipment.setStatus(ShipmentStatus.PLANNED); // Correct initial status
    }

    // ========================================================
    // 1. dispatchShipment Tests (The Complex Logic)
    // ========================================================



    @Test
    @DisplayName("Dispatch Success: Should update stock, create movement, and update statuses")
    void dispatchShipment_Success() {
        // Given
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));
        when(inventoryRepository.findByProductAndWarehouse(mockProduct, mockWarehouse))
                .thenReturn(Optional.of(mockInventory));

        // Mock Saving
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(mockShipment);

        // When
        ShipmentDTO result = shipmentService.dispatchShipment(1L);

        // Then
        assertNotNull(result);

        // 1. Verify Status Updates
        assertEquals(ShipmentStatus.IN_TRANSIT, mockShipment.getStatus());
        assertNotNull(mockShipment.getShippedAt());
        assertEquals(SOStatus.SHIPPED, mockOrder.getStatus());

        // 2. Verify Inventory Calculations
        // Initial: 50 OnHand, 10 Reserved. Shipped: 5.
        // Expected: 45 OnHand, 5 Reserved.
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        Inventory savedInv = inventoryCaptor.getValue();
        assertEquals(45L, savedInv.getQuantityOnHand());
        assertEquals(5L, savedInv.getQuantityReserved());

        // 3. Verify Movement Log
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Dispatch Failure: Should throw exception if Shipment not found")
    void dispatchShipment_NotFound() {
        when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shipmentService.dispatchShipment(99L));
    }

    @Test
    @DisplayName("Dispatch Failure: Should throw BusinessException if SalesOrder is NULL (Your Critical Fix)")
    void dispatchShipment_NullSalesOrder() {
        // Corrupt data: Shipment exists but has no order
        mockShipment.setSalesOrder(null);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> shipmentService.dispatchShipment(1L));

        assertTrue(ex.getMessage().contains("is not linked to any SalesOrder"));
    }

    @Test
    @DisplayName("Dispatch Failure: Should throw BusinessException if Warehouse is NULL")
    void dispatchShipment_NullWarehouse() {
        // Corrupt data: Order exists but has no source warehouse
        mockOrder.setWarehouse(null);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> shipmentService.dispatchShipment(1L));

        assertTrue(ex.getMessage().contains("not linked to a source Warehouse"));
    }

    @Test
    @DisplayName("Dispatch Failure: Should throw exception if Status is not PLANNED")
    void dispatchShipment_WrongStatus() {
        mockShipment.setStatus(ShipmentStatus.DELIVERED); // Wrong status
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));

        assertThrows(BusinessException.class,
                () -> shipmentService.dispatchShipment(1L));
    }

    @Test
    @DisplayName("Dispatch Failure: Should throw StockUnavailableException if Reserved < Required")
    void dispatchShipment_ConcurrencyConflict() {
        // Setup Concurrency Issue:
        // Line needs 5, but Inventory only has 2 reserved (maybe another process took them)
        mockInventory.setQuantityReserved(2L);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));
        when(inventoryRepository.findByProductAndWarehouse(mockProduct, mockWarehouse))
                .thenReturn(Optional.of(mockInventory));

        // Expect failure
        assertThrows(StockUnavailableException.class,
                () -> shipmentService.dispatchShipment(1L));

        // Ensure we did NOT save invalid data
        verify(inventoryRepository, never()).save(any());
        verify(inventoryMovementRepository, never()).save(any());
    }

    // ========================================================
    // 2. deliverShipment Tests
    // ========================================================

    @Test
    @DisplayName("Deliver Success: Should update statuses to DELIVERED")
    void deliverShipment_Success() {
        // Given: Correct state for delivery
        mockShipment.setStatus(ShipmentStatus.IN_TRANSIT);
        mockOrder.setStatus(SOStatus.SHIPPED);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(mockShipment);

        // When
        ShipmentDTO result = shipmentService.deliverShipment(1L);

        // Then
        assertEquals(ShipmentStatus.DELIVERED, mockShipment.getStatus());
        assertEquals(SOStatus.DELIVERED, mockOrder.getStatus());
        assertNotNull(mockShipment.getDeliveredAt());

        verify(salesOrderRepository).save(mockOrder);
        verify(shipmentRepository).save(mockShipment);
    }

    @Test
    @DisplayName("Deliver Failure: Should throw exception if Shipment not IN_TRANSIT")
    void deliverShipment_WrongShipmentStatus() {
        mockShipment.setStatus(ShipmentStatus.PLANNED); // Still planned, can't deliver yet
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));

        assertThrows(BusinessException.class,
                () -> shipmentService.deliverShipment(1L));
    }

    @Test
    @DisplayName("Deliver Failure: Should throw exception if Order not SHIPPED")
    void deliverShipment_WrongOrderStatus() {
        mockShipment.setStatus(ShipmentStatus.IN_TRANSIT);
        mockOrder.setStatus(SOStatus.RESERVED); // Mismatch logic
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> shipmentService.deliverShipment(1L));

        assertTrue(ex.getMessage().contains("SalesOrder status is not SHIPPED"));
    }
}