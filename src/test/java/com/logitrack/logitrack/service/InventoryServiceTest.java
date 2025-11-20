package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.AllocationDto;
import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.dto.Inventory.RequestInventoryDto;
import com.logitrack.logitrack.dto.Inventory.ResponseInventoryDto;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.MovementType;
import com.logitrack.logitrack.entity.enums.SOStatus;
import com.logitrack.logitrack.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private InventoryMovementRepository movementRepository;
    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;
    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product mockProduct;
    private Warehouse mockWarehouse;
    private Inventory mockInventory;
    private SalesOrderLine mockLine;
    private SalesOrder mockOrder;

    @BeforeEach
    void setUp() {
        // إعداد البيانات الأساسية
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Test Product");

        mockWarehouse = new Warehouse();
        mockWarehouse.setId(1L);
        mockWarehouse.setName("Main Warehouse");

        mockInventory = Inventory.builder()
                .id(1L)
                .product(mockProduct)
                .warehouse(mockWarehouse)
                .quantityOnHand(100L)
                .quantityReserved(10L)
                .movements(new ArrayList<>())
                .build();

        mockOrder = new SalesOrder();
        mockOrder.setId(100L);
        mockOrder.setStatus(SOStatus.CREATED);
        mockOrder.setLines(new ArrayList<>());

        mockLine = new SalesOrderLine();
        mockLine.setId(1L);
        mockLine.setProduct(mockProduct);
        mockLine.setRemainingQuantityToReserve(50L);
        mockLine.setSalesOrder(mockOrder);

        mockOrder.getLines().add(mockLine);
    }

    // --- Tests for addQtyOnHand ---

    @Test
    @DisplayName("addQtyOnHand: Should update quantity")
    void addQtyOnHand_Success() {
        RequestAddQtyOnHandDto dto = new RequestAddQtyOnHandDto();
        dto.setProductId(1L);
        dto.setWarehouseId(1);
        dto.setQuantityOnHand(50L);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1))
                .thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        Inventory result = inventoryService.addQtyOnHand(dto);

        assertEquals(150L, result.getQuantityOnHand());
        verify(inventoryRepository).save(mockInventory);
    }

    @Test
    void addQtyOnHand_NotFound() {
        RequestAddQtyOnHandDto dto = new RequestAddQtyOnHandDto();
        dto.setProductId(1L);
        dto.setWarehouseId(1);
        dto.setQuantityOnHand(50L);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> inventoryService.addQtyOnHand(dto));
    }

    // --- Tests for creatInventory ---

    @Test
    void creatInventory_Success() {
        RequestInventoryDto dto = new RequestInventoryDto();
        dto.setProductId(1L);
        dto.setWarehouseId(1L);

        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(mockWarehouse));
        when(modelMapper.map(any(), eq(Inventory.class))).thenReturn(mockInventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(mockInventory);
        when(modelMapper.map(any(), eq(ResponseInventoryDto.class))).thenReturn(new ResponseInventoryDto());

        ResponseInventoryDto result = inventoryService.creatInventory(dto);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void creatInventory_AlreadyExists() {
        RequestInventoryDto dto = new RequestInventoryDto();
        dto.setProductId(1L);
        dto.setWarehouseId(1L);

        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L)).thenReturn(Optional.of(mockInventory));

        assertThrows(RuntimeException.class, () -> inventoryService.creatInventory(dto));

        // تأكد من عدم استدعاء أي شيء آخر لتجنب UnnecessaryStubbing
        verify(productRepository, never()).findById(any());
    }

    // --- Tests for reserveProduct ---

    @Test
    void reserveProduct_Success() {
        mockInventory.setQuantityOnHand(100L);
        mockInventory.setQuantityReserved(20L); // Available = 80

        when(inventoryRepository.findAvailableStockForProduct(1L)).thenReturn(Collections.singletonList(mockInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(mockInventory);

        List<AllocationDto> result = inventoryService.reserveProduct(1L, 50L);

        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getAllocatedQuantity());
        assertEquals(70L, mockInventory.getQuantityReserved());
    }

    @Test
    void reserveProduct_Insufficient() {
        mockInventory.setQuantityOnHand(50L);
        mockInventory.setQuantityReserved(40L); // Available = 10

        when(inventoryRepository.findAvailableStockForProduct(1L)).thenReturn(Collections.singletonList(mockInventory));

        assertThrows(RuntimeException.class, () -> inventoryService.reserveProduct(1L, 50L));
    }

    // --- Tests for receiveStockAndFulfillBackorders ---

    @Test
    void receiveStock_FulfillBackorders() {
        // Given
        Long quantityReceived = 100L;
        // MockInventory state: OnHand=100

        // 1. Mock Finding Inventory
        when(inventoryRepository.findByProductAndWarehouse(mockProduct, mockWarehouse))
                .thenReturn(Optional.of(mockInventory));

        // 2. Mock Saving Inventory (Capture arguments to fix Assertion error)
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        // 3. Mock Saving Movement
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(new InventoryMovement());

        // 4. Mock Global Stock Calculation
        // The service calls getGlobalAvailableStock internally which calls findByProductId
        // Note: The service updates the inventory object in memory first, then calls this.
        // We need to return the updated inventory list.
        lenient().when(inventoryRepository.findByProductId(1L)).thenReturn(Collections.singletonList(mockInventory));

        // 5. Mock Finding Backorders
        lenient().when(salesOrderLineRepository.findBackordersForProduct(1L)).thenReturn(Collections.singletonList(mockLine));

        // 6. Mock Reserve (Internal Call)
        lenient().when(inventoryRepository.findAvailableStockForProduct(1L)).thenReturn(Collections.singletonList(mockInventory));

        // 7. Mock Order
        lenient().when(salesOrderRepository.findByIdWithLinesAndProducts(100L)).thenReturn(Optional.of(mockOrder));

        // When
        inventoryService.receiveStockAndFulfillBackorders(mockProduct, mockWarehouse, quantityReceived);

        // Then
        // Check if stock increased correctly (100 original + 100 received = 200)
        assertEquals(200L, mockInventory.getQuantityOnHand());

        verify(salesOrderLineRepository).save(mockLine);
        verify(salesOrderRepository).save(mockOrder);
    }

    @Test
    void receiveStock_NewInventory() {
        // Given
        Long quantityReceived = 50L;

        // Mock finding returns empty (New Inventory case)
        when(inventoryRepository.findByProductAndWarehouse(mockProduct, mockWarehouse))
                .thenReturn(Optional.empty());

        // Mock saving movement
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(new InventoryMovement());

        // Fix for "Wanted but not invoked": Use ArgumentCaptor
        // The service creates a NEW inventory object, so we can't match strictly.
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);

        // Mock dependencies for global stock check (to avoid null pointers later in the method)
        lenient().when(inventoryRepository.findByProductId(1L)).thenReturn(new ArrayList<>());
        lenient().when(salesOrderLineRepository.findBackordersForProduct(1L)).thenReturn(Collections.emptyList());

        // When
        inventoryService.receiveStockAndFulfillBackorders(mockProduct, mockWarehouse, quantityReceived);

        // Then
        // Capture the saved inventory and inspect it
        verify(inventoryRepository).save(inventoryCaptor.capture());
        Inventory savedInventory = inventoryCaptor.getValue();

        assertEquals(50L, savedInventory.getQuantityOnHand());
        assertEquals(mockProduct, savedInventory.getProduct());
        assertEquals(mockWarehouse, savedInventory.getWarehouse());
    }

    // --- Helper Methods ---

    @Test
    void getGlobalAvailableStock_Test() {
        Inventory inv1 = Inventory.builder().quantityOnHand(100L).quantityReserved(20L).build();
        Inventory inv2 = Inventory.builder().quantityOnHand(50L).quantityReserved(10L).build();

        when(inventoryRepository.findByProductId(1L)).thenReturn(Arrays.asList(inv1, inv2));

        long result = inventoryService.getGlobalAvailableStock(1L);

        // (100+50) - (20+10) = 150 - 30 = 120
        assertEquals(120L, result);
    }

    @Test
    void chectQuentutProduct_Test() {
        // Case 1: Not found -> returns false
        when(inventoryRepository.findByProduct(mockProduct)).thenReturn(null);
        assertFalse(inventoryService.chectQuentutProduct(mockProduct));

        // Case 2: Has Quantity (10) -> returns false (based on your logic: if > 0 return false)
        mockInventory.setQuantityOnHand(10L);
        when(inventoryRepository.findByProduct(mockProduct)).thenReturn(mockInventory);
        assertFalse(inventoryService.chectQuentutProduct(mockProduct));

        // Case 3: Zero Quantity -> returns true
        mockInventory.setQuantityOnHand(0L);
        // IMPORTANT: We must mock the return again because the object state changed
        when(inventoryRepository.findByProduct(mockProduct)).thenReturn(mockInventory);
        assertTrue(inventoryService.chectQuentutProduct(mockProduct));
    }
}