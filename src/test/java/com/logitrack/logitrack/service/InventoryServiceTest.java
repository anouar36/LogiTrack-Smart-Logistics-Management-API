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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
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

    private Product testProduct;
    private Warehouse testWarehouse;
    private Inventory testInventory;
    private RequestInventoryDto requestInventoryDto;
    private ResponseInventoryDto responseInventoryDto;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .sku("TEST-SKU-001")
                .name("Test Product")
                .category("Electronics")
                .price(BigDecimal.valueOf(99.99))
                .active(true)
                .deleted(false)
                .build();        testWarehouse = Warehouse.builder()
                .id(1L)
                .name("Main Warehouse")
                .code("WH001")
                .build();

        testInventory = Inventory.builder()
                .id(1L)
                .product(testProduct)
                .warehouse(testWarehouse)
                .quantityOnHand(100L)
                .quantityReserved(10L)
                .movements(new ArrayList<>())
                .build();        requestInventoryDto = new RequestInventoryDto();
        requestInventoryDto.setProductId(1L);
        requestInventoryDto.setWarehouseId(1L);
        requestInventoryDto.setQuantityOnHand(100L);
        requestInventoryDto.setQuantityReserved(0L);

        responseInventoryDto = ResponseInventoryDto.builder()
                .id(1L)
                .productId(1L)
                .warehouseId(1L)
                .quantityOnHand(100L)
                .quantityReserved(0L)
                .build();
    }    @Test
    void addQtyOnHand_WhenInventoryExists_ShouldUpdateQuantity() {
        // Given
        RequestAddQtyOnHandDto dto = new RequestAddQtyOnHandDto();
        dto.setProductId(1L);
        dto.setWarehouseId(1);
        dto.setQuantityOnHand(50L);        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);

        // When
        Inventory result = inventoryService.addQtyOnHand(dto);

        // Then
        assertNotNull(result);
        assertEquals(150L, result.getQuantityOnHand()); // 100 + 50
        verify(inventoryRepository).findByProductIdAndWarehouseId(1L, 1);
        verify(inventoryRepository).save(testInventory);
    }    @Test
    void addQtyOnHand_WhenInventoryNotExists_ShouldThrowException() {
        // Given
        RequestAddQtyOnHandDto dto = new RequestAddQtyOnHandDto();
        dto.setProductId(1L);
        dto.setWarehouseId(1);
        dto.setQuantityOnHand(50L);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.addQtyOnHand(dto));

        assertEquals("Inventory does not exist,Please, can you create new Inventory?", 
                exception.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_WhenInventoryDoesNotExist_ShouldCreateNew() {
        // Given
        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(modelMapper.map(requestInventoryDto, Inventory.class)).thenReturn(testInventory);
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);
        when(modelMapper.map(testInventory, ResponseInventoryDto.class)).thenReturn(responseInventoryDto);

        // When
        ResponseInventoryDto result = inventoryService.creatInventory(requestInventoryDto);

        // Then
        assertNotNull(result);
        assertEquals(responseInventoryDto.getId(), result.getId());
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void createInventory_WhenInventoryAlreadyExists_ShouldThrowException() {
        // Given
        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(testInventory));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.creatInventory(requestInventoryDto));

        assertEquals("Inventory already exists for this product in this warehouse", 
                exception.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void createInventory_WhenProductNotFound_ShouldThrowException() {
        // Given
        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.creatInventory(requestInventoryDto));

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void createInventory_WhenWarehouseNotFound_ShouldThrowException() {
        // Given
        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.creatInventory(requestInventoryDto));

        assertEquals("Warehouse not found", exception.getMessage());
    }

    @Test
    void reserveProduct_WhenSufficientStock_ShouldReserveQuantity() {
        // Given
        Long productId = 1L;
        Long quantityNeeded = 50L;
        
        testInventory.setQuantityOnHand(100L);
        testInventory.setQuantityReserved(20L); // Available: 80
        
        List<Inventory> inventories = Arrays.asList(testInventory);
        when(inventoryRepository.findAvailableStockForProduct(productId))
                .thenReturn(inventories);
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);

        // When
        List<AllocationDto> result = inventoryService.reserveProduct(productId, quantityNeeded);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getWarehouseId());
        assertEquals(50L, result.get(0).getAllocatedQuantity());
        assertEquals(70L, testInventory.getQuantityReserved()); // 20 + 50
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void reserveProduct_WhenInsufficientStock_ShouldThrowException() {
        // Given
        Long productId = 1L;
        Long quantityNeeded = 100L;
        
        testInventory.setQuantityOnHand(50L);
        testInventory.setQuantityReserved(10L); // Available: 40
        
        List<Inventory> inventories = Arrays.asList(testInventory);
        when(inventoryRepository.findAvailableStockForProduct(productId))
                .thenReturn(inventories);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.reserveProduct(productId, quantityNeeded));

        assertTrue(exception.getMessage().contains("Unable to reserve the full quantity"));
    }

    @Test
    void reserveProduct_WhenNoAvailableStock_ShouldThrowException() {
        // Given
        Long productId = 1L;
        Long quantityNeeded = 50L;
        
        when(inventoryRepository.findAvailableStockForProduct(productId))
                .thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.reserveProduct(productId, quantityNeeded));

        assertEquals("No available stock found for product: " + productId, exception.getMessage());
    }

    @Test
    void receiveStockAndFulfillBackorders_WhenInventoryExists_ShouldUpdateStock() {
        // Given
        Long quantityReceived = 50L;
        
        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);
        when(movementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(salesOrderLineRepository.findBackordersForProduct(testProduct.getId()))
                .thenReturn(Collections.emptyList());

        // When
        inventoryService.receiveStockAndFulfillBackorders(testProduct, testWarehouse, quantityReceived);

        // Then
        assertEquals(150L, testInventory.getQuantityOnHand()); // 100 + 50
        verify(inventoryRepository).save(testInventory);
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void receiveStockAndFulfillBackorders_WhenInventoryNotExists_ShouldCreateNew() {
        // Given
        Long quantityReceived = 50L;
        
        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inventory = invocation.getArgument(0);
            inventory.setId(1L);
            return inventory;
        });
        when(movementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(salesOrderLineRepository.findBackordersForProduct(testProduct.getId()))
                .thenReturn(Collections.emptyList());

        // When
        inventoryService.receiveStockAndFulfillBackorders(testProduct, testWarehouse, quantityReceived);

        // Then
        verify(inventoryRepository).save(any(Inventory.class));
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void receiveStockAndFulfillBackorders_WithBackorders_ShouldFulfillPartially() {
        // Given
        Long quantityReceived = 50L;
        
        SalesOrder salesOrder = SalesOrder.builder()
                .id(1L)
                .status(SOStatus.CREATED)
                .build();
        
        SalesOrderLine backorderLine = SalesOrderLine.builder()
                .id(1L)
                .product(testProduct)
                .salesOrder(salesOrder)
                .quantity(30L)
                .remainingQuantityToReserve(30L)
                .build();
        
        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);
        when(movementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(salesOrderLineRepository.findBackordersForProduct(testProduct.getId()))
                .thenReturn(Arrays.asList(backorderLine));
        
        // Mock the getGlobalAvailableStock method by setting up inventory properly
        List<Inventory> inventoriesForGlobalStock = Arrays.asList(testInventory);
        when(inventoryRepository.findByProductId(testProduct.getId()))
                .thenReturn(inventoriesForGlobalStock);
        
        // Mock reserveProduct call for backorder fulfillment
        when(inventoryRepository.findAvailableStockForProduct(testProduct.getId()))
                .thenReturn(Arrays.asList(testInventory));
        
        when(salesOrderLineRepository.save(backorderLine)).thenReturn(backorderLine);
        when(salesOrderRepository.findByIdWithLinesAndProducts(1L))
                .thenReturn(Optional.of(salesOrder));

        // When
        inventoryService.receiveStockAndFulfillBackorders(testProduct, testWarehouse, quantityReceived);

        // Then
        verify(salesOrderLineRepository).save(backorderLine);
        assertEquals(150L, testInventory.getQuantityOnHand()); // 100 + 50
    }

    @Test
    void getGlobalAvailableStock_ShouldCalculateCorrectlyStock() {
        // Given
        Long productId = 1L;
        
        Inventory inventory1 = Inventory.builder()
                .quantityOnHand(100L)
                .quantityReserved(20L)
                .build();
        
        Inventory inventory2 = Inventory.builder()
                .quantityOnHand(150L)
                .quantityReserved(30L)
                .build();
        
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);
        when(inventoryRepository.findByProductId(productId)).thenReturn(inventories);

        // When
        long result = inventoryService.getGlobalAvailableStock(productId);

        // Then
        assertEquals(200L, result); // (100 + 150) - (20 + 30) = 200
        verify(inventoryRepository).findByProductId(productId);
    }

    @Test
    void checkQuantityProduct_WhenInventoryExistsWithQuantity_ShouldReturnFalse() {
        // Given
        testInventory.setQuantityOnHand(10L);
        when(inventoryRepository.findByProduct(testProduct)).thenReturn(testInventory);

        // When
        boolean result = inventoryService.chectQuentutProduct(testProduct);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByProduct(testProduct);
    }

    @Test
    void checkQuantityProduct_WhenInventoryExistsWithoutQuantity_ShouldReturnTrue() {
        // Given
        testInventory.setQuantityOnHand(0L);
        when(inventoryRepository.findByProduct(testProduct)).thenReturn(testInventory);

        // When
        boolean result = inventoryService.chectQuentutProduct(testProduct);

        // Then
        assertTrue(result);
        verify(inventoryRepository).findByProduct(testProduct);
    }

    @Test
    void checkQuantityProduct_WhenInventoryNotExists_ShouldReturnFalse() {
        // Given
        when(inventoryRepository.findByProduct(testProduct)).thenReturn(null);

        // When
        boolean result = inventoryService.chectQuentutProduct(testProduct);

        // Then
        assertFalse(result);
        verify(inventoryRepository).findByProduct(testProduct);
    }

    @Test
    void receiveStockAndFulfillBackorders_WhenBackorderFullyFulfilled_ShouldUpdateOrderStatus() {
        // Given
        Long quantityReceived = 100L;
        
        SalesOrder salesOrder = SalesOrder.builder()
                .id(1L)
                .status(SOStatus.CREATED)
                .lines(new ArrayList<>())
                .build();
        
        SalesOrderLine backorderLine = SalesOrderLine.builder()
                .id(1L)
                .product(testProduct)
                .salesOrder(salesOrder)
                .quantity(30L)
                .remainingQuantityToReserve(30L)
                .build();
        
        salesOrder.getLines().add(backorderLine);
        
        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);
        when(movementRepository.save(any(InventoryMovement.class)))
                .thenReturn(new InventoryMovement());
        when(salesOrderLineRepository.findBackordersForProduct(testProduct.getId()))
                .thenReturn(Arrays.asList(backorderLine));
        
        // Mock global available stock calculation
        when(inventoryRepository.findByProductId(testProduct.getId()))
                .thenReturn(Arrays.asList(testInventory));
        
        // Mock reserveProduct for backorder fulfillment
        when(inventoryRepository.findAvailableStockForProduct(testProduct.getId()))
                .thenReturn(Arrays.asList(testInventory));
        
        when(salesOrderLineRepository.save(backorderLine)).thenReturn(backorderLine);
        when(salesOrderRepository.findByIdWithLinesAndProducts(1L))
                .thenReturn(Optional.of(salesOrder));
        when(salesOrderRepository.save(salesOrder)).thenReturn(salesOrder);

        // When
        inventoryService.receiveStockAndFulfillBackorders(testProduct, testWarehouse, quantityReceived);

        // Then
        verify(salesOrderLineRepository).save(backorderLine);
        verify(salesOrderRepository).save(salesOrder);
        assertEquals(SOStatus.RESERVED, salesOrder.getStatus());
    }
}
