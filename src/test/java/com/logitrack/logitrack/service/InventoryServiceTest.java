package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.AllocationDto;
import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.dto.Inventory.RequestInventoryDto;
import com.logitrack.logitrack.dto.Inventory.ResponseInventoryDto;
import com.logitrack.logitrack.entity.*;
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
                .build();

        testWarehouse = Warehouse.builder()
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
                .build();

        requestInventoryDto = new RequestInventoryDto();
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
    }

    @Test
    void addQtyOnHand_WhenInventoryExists_ShouldUpdateQuantity() {
        RequestAddQtyOnHandDto dto = new RequestAddQtyOnHandDto();
        dto.setProductId(1L);
        dto.setWarehouseId(1);
        dto.setQuantityOnHand(50L);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);

        Inventory result = inventoryService.addQtyOnHand(dto);

        assertNotNull(result);
        assertEquals(150L, result.getQuantityOnHand());
        verify(inventoryRepository).findByProductIdAndWarehouseId(1L, 1);
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void addQtyOnHand_WhenInventoryNotExists_ShouldThrowException() {
        RequestAddQtyOnHandDto dto = new RequestAddQtyOnHandDto();
        dto.setProductId(1L);
        dto.setWarehouseId(1);
        dto.setQuantityOnHand(50L);

        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.addQtyOnHand(dto));

        assertEquals("Inventory does not exist,Please, can you create new Inventory?",
                exception.getMessage());
    }

    @Test
    void createInventory_WhenInventoryDoesNotExist_ShouldCreateNew() {
        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(modelMapper.map(requestInventoryDto, Inventory.class)).thenReturn(testInventory);
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);
        when(modelMapper.map(testInventory, ResponseInventoryDto.class)).thenReturn(responseInventoryDto);

        ResponseInventoryDto result = inventoryService.creatInventory(requestInventoryDto);

        assertNotNull(result);
        assertEquals(responseInventoryDto.getId(), result.getId());
    }

    @Test
    void createInventory_WhenInventoryAlreadyExists_ShouldThrowException() {
        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(testInventory));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.creatInventory(requestInventoryDto));

        assertEquals("Inventory already exists for this product in this warehouse",
                exception.getMessage());
    }

    @Test
    void createInventory_WhenProductNotFound_ShouldThrowException() {
        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.creatInventory(requestInventoryDto));

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void createInventory_WhenWarehouseNotFound_ShouldThrowException() {
        when(inventoryRepository.existsByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.creatInventory(requestInventoryDto));

        assertEquals("Warehouse not found", exception.getMessage());
    }

    @Test
    void reserveProduct_WhenSufficientStock_ShouldReserveQuantity() {
        Long productId = 1L;
        Long quantityNeeded = 50L;
        testInventory.setQuantityOnHand(100L);
        testInventory.setQuantityReserved(20L);

        List<Inventory> inventories = Arrays.asList(testInventory);
        when(inventoryRepository.findAvailableStockForProduct(productId)).thenReturn(inventories);
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);

        List<AllocationDto> result = inventoryService.reserveProduct(productId, quantityNeeded);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getAllocatedQuantity());
        assertEquals(70L, testInventory.getQuantityReserved());
    }

    @Test
    void reserveProduct_WhenInsufficientStock_ShouldThrowException() {
        Long productId = 1L;
        Long quantityNeeded = 100L;
        testInventory.setQuantityOnHand(50L);
        testInventory.setQuantityReserved(10L);

        List<Inventory> inventories = Arrays.asList(testInventory);
        when(inventoryRepository.findAvailableStockForProduct(productId)).thenReturn(inventories);

        assertThrows(RuntimeException.class,
                () -> inventoryService.reserveProduct(productId, quantityNeeded));
    }

    @Test
    void reserveProduct_WhenNoAvailableStock_ShouldThrowException() {
        Long productId = 1L;
        Long quantityNeeded = 50L;
        when(inventoryRepository.findAvailableStockForProduct(productId)).thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> inventoryService.reserveProduct(productId, quantityNeeded));

        assertEquals("No available stock found for product: " + productId, exception.getMessage());
    }

    @Test
    void receiveStockAndFulfillBackorders_WhenInventoryExists_ShouldUpdateStock() {
        Long quantityReceived = 50L;

        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(new InventoryMovement());
        
        // Added lenient to fix UnnecessaryStubbing
        lenient().when(salesOrderLineRepository.findBackordersForProduct(testProduct.getId()))
                .thenReturn(Collections.emptyList());

        inventoryService.receiveStockAndFulfillBackorders(testProduct, testWarehouse, quantityReceived);

        assertEquals(150L, testInventory.getQuantityOnHand());
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void receiveStockAndFulfillBackorders_WhenInventoryNotExists_ShouldCreateNew() {
        Long quantityReceived = 50L;

        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inventory = invocation.getArgument(0);
            inventory.setId(1L);
            return inventory;
        });
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(new InventoryMovement());
        
        // Added lenient to fix UnnecessaryStubbing
        lenient().when(salesOrderLineRepository.findBackordersForProduct(testProduct.getId()))
                .thenReturn(Collections.emptyList());

        inventoryService.receiveStockAndFulfillBackorders(testProduct, testWarehouse, quantityReceived);

        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void receiveStockAndFulfillBackorders_WithBackorders_ShouldFulfillPartially() {
        Long quantityReceived = 50L;
        SalesOrderLine backorderLine = SalesOrderLine.builder()
                .id(1L).product(testProduct).quantity(30L).remainingQuantityToReserve(30L).build();
        
        // Fixed NullPointerException by initializing ArrayList
        SalesOrder salesOrder = SalesOrder.builder()
                .id(1L).status(SOStatus.CREATED).lines(new ArrayList<>(Arrays.asList(backorderLine))).build();
        backorderLine.setSalesOrder(salesOrder);

        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(new InventoryMovement());
        
        lenient().when(salesOrderLineRepository.findBackordersForProduct(testProduct.getId()))
                .thenReturn(Arrays.asList(backorderLine));
        lenient().when(inventoryRepository.findByProductId(testProduct.getId()))
                .thenReturn(Arrays.asList(testInventory));
        lenient().when(inventoryRepository.findAvailableStockForProduct(testProduct.getId()))
                .thenReturn(Arrays.asList(testInventory));
        when(salesOrderLineRepository.save(backorderLine)).thenReturn(backorderLine);
        when(salesOrderRepository.findByIdWithLinesAndProducts(1L)).thenReturn(Optional.of(salesOrder));

        inventoryService.receiveStockAndFulfillBackorders(testProduct, testWarehouse, quantityReceived);

        verify(salesOrderLineRepository).save(backorderLine);
        assertEquals(150L, testInventory.getQuantityOnHand());
    }

    @Test
    void getGlobalAvailableStock_ShouldCalculateCorrectlyStock() {
        Long productId = 1L;
        Inventory inventory1 = Inventory.builder().quantityOnHand(100L).quantityReserved(20L).build();
        Inventory inventory2 = Inventory.builder().quantityOnHand(150L).quantityReserved(30L).build();
        when(inventoryRepository.findByProductId(productId)).thenReturn(Arrays.asList(inventory1, inventory2));

        long result = inventoryService.getGlobalAvailableStock(productId);

        assertEquals(200L, result);
    }

    @Test
    void checkQuantityProduct_WhenInventoryExistsWithQuantity_ShouldReturnFalse() {
        testInventory.setQuantityOnHand(10L);
        when(inventoryRepository.findByProduct(testProduct)).thenReturn(testInventory);
        boolean result = inventoryService.chectQuentutProduct(testProduct);
        assertFalse(result);
    }

    @Test
    void checkQuantityProduct_WhenInventoryExistsWithoutQuantity_ShouldReturnTrue() {
        testInventory.setQuantityOnHand(0L);
        when(inventoryRepository.findByProduct(testProduct)).thenReturn(testInventory);
        boolean result = inventoryService.chectQuentutProduct(testProduct);
        assertTrue(result);
    }

    @Test
    void checkQuantityProduct_WhenInventoryNotExists_ShouldReturnFalse() {
        when(inventoryRepository.findByProduct(testProduct)).thenReturn(null);
        boolean result = inventoryService.chectQuentutProduct(testProduct);
        assertFalse(result);
    }
    
    @Test
    void receiveStockAndFulfillBackorders_WhenBackorderFullyFulfilled_ShouldUpdateOrderStatus() {
        Long quantityReceived = 100L;
        SalesOrder salesOrder = SalesOrder.builder()
                .id(1L).status(SOStatus.CREATED).lines(new ArrayList<>()).build();
        SalesOrderLine backorderLine = SalesOrderLine.builder()
                .id(1L).product(testProduct).salesOrder(salesOrder).quantity(30L).remainingQuantityToReserve(30L).build();
        salesOrder.getLines().add(backorderLine);

        when(inventoryRepository.findByProductAndWarehouse(testProduct, testWarehouse))
                .thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(testInventory)).thenReturn(testInventory);
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(new InventoryMovement());
        lenient().when(salesOrderLineRepository.findBackordersForProduct(testProduct.getId()))
                .thenReturn(Arrays.asList(backorderLine));
        lenient().when(inventoryRepository.findByProductId(testProduct.getId()))
                .thenReturn(Arrays.asList(testInventory));
        lenient().when(inventoryRepository.findAvailableStockForProduct(testProduct.getId()))
                .thenReturn(Arrays.asList(testInventory));
        when(salesOrderLineRepository.save(backorderLine)).thenReturn(backorderLine);
        when(salesOrderRepository.findByIdWithLinesAndProducts(1L)).thenReturn(Optional.of(salesOrder));
        when(salesOrderRepository.save(salesOrder)).thenReturn(salesOrder);

        inventoryService.receiveStockAndFulfillBackorders(testProduct, testWarehouse, quantityReceived);

        verify(salesOrderRepository).save(salesOrder);
        assertEquals(SOStatus.RESERVED, salesOrder.getStatus());
    }
}