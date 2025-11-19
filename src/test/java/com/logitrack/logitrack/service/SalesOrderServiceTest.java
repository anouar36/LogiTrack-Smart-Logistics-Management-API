package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.AllocationDto;
import com.logitrack.logitrack.dto.Product.AddProductToOrderRequest;
import com.logitrack.logitrack.dto.SalesOrder.ResponceSalesOrderDto;
import com.logitrack.logitrack.dto.SalesOrder.ResponseSalesOrderLineDto;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.SOStatus;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.mapper.DesplayAllOrdersLineDtosMapper;
import com.logitrack.logitrack.repository.SalesOrderLineRepository;
import com.logitrack.logitrack.repository.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private SalesOrderLineService salesOrderLineService;

    @Mock
    private ProductService productService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ClientService clientService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private DesplayAllOrdersLineDtosMapper desplayAllOrdersLineDtosMapper;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private SalesOrder mockOrder;
    private Product mockProduct;
    private Client mockClient;
    private User mockUser;
    private AddProductToOrderRequest mockProductRequest;
    private AllocationDto mockAllocation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        // Setup mock client
        mockClient = new Client();
        mockClient.setId(1L);
        mockClient.setName("Test Client");
        mockClient.setUser(mockUser);

        // Setup mock product
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Test Product");
        mockProduct.setPrice(BigDecimal.valueOf(10.00));
        mockProduct.setActive(true);

        // Setup mock order
        mockOrder = new SalesOrder();
        mockOrder.setId(1L);
        mockOrder.setClient(mockClient);
        mockOrder.setStatus(SOStatus.CREATED);
        mockOrder.setCreatedAt(LocalDateTime.now());
        // IMPORTANT: Initialize list to avoid NullPointerException
        mockOrder.setLines(new ArrayList<>()); 

        // Setup mock product request
        mockProductRequest = new AddProductToOrderRequest();
        mockProductRequest.setProductId(1L);
        mockProductRequest.setQuantity(5L);

        // Setup mock allocation
        mockAllocation = new AllocationDto();
        mockAllocation.setAllocatedQuantity(5L);
    }

    @Test
    void addProductsToOrder_Success_FullyReserved() {
        // Arrange
        Long orderId = 1L;
        List<AddProductToOrderRequest> productsToAdd = Arrays.asList(mockProductRequest);
        List<AllocationDto> allocations = Arrays.asList(mockAllocation);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(productService.getProductById(1L)).thenReturn(Optional.of(mockProduct));
        when(inventoryService.reserveProduct(1L, 5L)).thenReturn(allocations);
        when(salesOrderLineRepository.save(any(SalesOrderLine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);
        
        // Use lenient for mapper in loop
        lenient().when(modelMapper.map(any(SalesOrderLine.class), eq(ResponseSalesOrderLineDto.class)))
                .thenReturn(new ResponseSalesOrderLineDto());

        // Act
        ResponceSalesOrderDto result = salesOrderService.addProductsToOrder(orderId, productsToAdd);

        // Assert
        assertNotNull(result);
        assertEquals(mockClient.getId(), result.getClientId());
        assertEquals(SOStatus.CREATED, result.getStatus());
        assertTrue(result.getMessage().contains("added and reserved"));
        assertEquals(BigDecimal.valueOf(50.00), result.getTotalPrice());

        verify(inventoryService).reserveProduct(1L, 5L);
    }

    @Test
    void addProductsToOrder_PartialReservation_BackorderCreated() {
        // Arrange
        Long orderId = 1L;
        List<AddProductToOrderRequest> productsToAdd = Arrays.asList(mockProductRequest);
        
        // Mock partial allocation (only 3 out of 5 units reserved)
        AllocationDto partialAllocation = new AllocationDto();
        partialAllocation.setAllocatedQuantity(3L);
        List<AllocationDto> allocations = Arrays.asList(partialAllocation);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(productService.getProductById(1L)).thenReturn(Optional.of(mockProduct));
        when(inventoryService.reserveProduct(1L, 5L)).thenReturn(allocations);
        when(salesOrderLineRepository.save(any(SalesOrderLine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);
        
        lenient().when(modelMapper.map(any(SalesOrderLine.class), eq(ResponseSalesOrderLineDto.class)))
                .thenReturn(new ResponseSalesOrderLineDto());

        // Act
        ResponceSalesOrderDto result = salesOrderService.addProductsToOrder(orderId, productsToAdd);

        // Assert
        assertNotNull(result);
        assertTrue(result.getMessage().contains("Backorder: 2 units"));
        assertEquals(BigDecimal.valueOf(50.00), result.getTotalPrice()); // Still charged full amount
    }

    @Test
    void addProductsToOrder_InactiveProduct_Skipped() {
        // Arrange
        Long orderId = 1L;
        Product inactiveProduct = new Product();
        inactiveProduct.setId(1L);
        inactiveProduct.setName("Inactive Product");
        inactiveProduct.setActive(false); // Set to Inactive
        
        List<AddProductToOrderRequest> productsToAdd = Arrays.asList(mockProductRequest);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(productService.getProductById(1L)).thenReturn(Optional.of(inactiveProduct));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);

        // Act
        ResponceSalesOrderDto result = salesOrderService.addProductsToOrder(orderId, productsToAdd);

        // Assert
        assertNotNull(result);
        assertTrue(result.getMessage().contains("is inactive and was skipped"));
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());

        // Verify inventory was NEVER called
        verify(inventoryService, never()).reserveProduct(anyLong(), anyLong());
        verify(salesOrderLineRepository, never()).save(any(SalesOrderLine.class));
    }

    @Test
    void addProductsToOrder_MultipleProducts_MixedResults() {
        // Arrange
        Long orderId = 1L;
        
        // Second product setup
        Product secondProduct = new Product();
        secondProduct.setId(2L);
        secondProduct.setName("Second Product");
        secondProduct.setPrice(BigDecimal.valueOf(15.00));
        secondProduct.setActive(true);

        AddProductToOrderRequest secondRequest = new AddProductToOrderRequest();
        secondRequest.setProductId(2L);
        secondRequest.setQuantity(3L);

        List<AddProductToOrderRequest> productsToAdd = Arrays.asList(mockProductRequest, secondRequest);

        // Allocations
        List<AllocationDto> firstAllocations = Arrays.asList(mockAllocation); // Full
        
        AllocationDto partialAllocation = new AllocationDto();
        partialAllocation.setAllocatedQuantity(2L);
        List<AllocationDto> secondAllocations = Arrays.asList(partialAllocation); // Partial

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(productService.getProductById(1L)).thenReturn(Optional.of(mockProduct));
        when(productService.getProductById(2L)).thenReturn(Optional.of(secondProduct));
        when(inventoryService.reserveProduct(1L, 5L)).thenReturn(firstAllocations);
        when(inventoryService.reserveProduct(2L, 3L)).thenReturn(secondAllocations);
        when(salesOrderLineRepository.save(any(SalesOrderLine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);
        
        lenient().when(modelMapper.map(any(SalesOrderLine.class), eq(ResponseSalesOrderLineDto.class)))
                .thenReturn(new ResponseSalesOrderLineDto());

        // Act
        ResponceSalesOrderDto result = salesOrderService.addProductsToOrder(orderId, productsToAdd);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(95.00), result.getTotalPrice()); // (5*10) + (3*15) = 50 + 45 = 95

        verify(inventoryService).reserveProduct(1L, 5L);
        verify(inventoryService).reserveProduct(2L, 3L);
        verify(salesOrderLineRepository, times(2)).save(any(SalesOrderLine.class));
    }

    @Test
    void addProductsToOrder_OrderNotFound_ThrowsException() {
        // Arrange
        Long orderId = 999L;
        List<AddProductToOrderRequest> productsToAdd = Arrays.asList(mockProductRequest);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> salesOrderService.addProductsToOrder(orderId, productsToAdd));

        verify(productService, never()).getProductById(anyLong());
    }

    @Test
    void addProductsToOrder_ProductNotFound_ThrowsException() {
        // Arrange
        Long orderId = 1L;
        List<AddProductToOrderRequest> productsToAdd = Arrays.asList(mockProductRequest);

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(productService.getProductById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> salesOrderService.addProductsToOrder(orderId, productsToAdd));
            
        verify(salesOrderLineRepository, never()).save(any(SalesOrderLine.class));
    }

    @Test
    void addProductsToOrder_EmptyProductList_ReturnsEmptyResult() {
        // Arrange
        Long orderId = 1L;
        List<AddProductToOrderRequest> productsToAdd = new ArrayList<>();

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);

        // Act
        ResponceSalesOrderDto result = salesOrderService.addProductsToOrder(orderId, productsToAdd);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        verify(productService, never()).getProductById(anyLong());
    }

    @Test
    void addProductsToOrder_NoAllocation_FullBackorder() {
        // Arrange
        Long orderId = 1L;
        List<AddProductToOrderRequest> productsToAdd = Arrays.asList(mockProductRequest);
        List<AllocationDto> emptyAllocations = new ArrayList<>();

        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(productService.getProductById(1L)).thenReturn(Optional.of(mockProduct));
        when(inventoryService.reserveProduct(1L, 5L)).thenReturn(emptyAllocations);
        when(salesOrderLineRepository.save(any(SalesOrderLine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(mockOrder);
        
        lenient().when(modelMapper.map(any(SalesOrderLine.class), eq(ResponseSalesOrderLineDto.class)))
                .thenReturn(new ResponseSalesOrderLineDto());

        // Act
        ResponceSalesOrderDto result = salesOrderService.addProductsToOrder(orderId, productsToAdd);

        // Assert
        assertNotNull(result);
        assertTrue(result.getMessage().contains("Backorder: 5 units"));
        assertEquals(BigDecimal.valueOf(50.00), result.getTotalPrice());
    }
}