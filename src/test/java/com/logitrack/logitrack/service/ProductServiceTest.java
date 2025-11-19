package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Product.ProductAvailabilityDto;
import com.logitrack.logitrack.dto.Product.RequestDTO;
import com.logitrack.logitrack.dto.Product.ResponseDTO;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.mapper.CreatProductMapper;
import com.logitrack.logitrack.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // ✅ هذا يكفي لتهيئة Mocks
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CreatProductMapper productMapper;
    @Mock
    private SalesOrderService salesOrderService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private SalesOrderLineService salesOrderLineService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private RequestDTO testRequestDTO;
    private ResponseDTO testResponseDTO;

    @BeforeEach
    void setUp() {
        // ❌ تم حذف: MockitoAnnotations.openMocks(this); لأنه يسبب المشكلة

        testProduct = Product.builder()
                .id(1L)
                .sku("SKU-123")
                .name("Laptop")
                .category("Electronics")
                .price(BigDecimal.valueOf(1000))
                .active(true)
                .deleted(false)
                .build();

        testRequestDTO = new RequestDTO();
        testRequestDTO.setSku("SKU-123");
        testRequestDTO.setName("Laptop");
        testRequestDTO.setCategory("Electronics");
        testRequestDTO.setPrice(BigDecimal.valueOf(1000));

        testResponseDTO = new ResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setSku("SKU-123");
    }

    @Test
    void addProducte_WhenProductExists_ShouldSave() {
        when(productRepository.existsByNameAndSku(any(), any())).thenReturn(true);
        when(productMapper.toEntity(any())).thenReturn(testProduct);
        when(productRepository.save(any())).thenReturn(testProduct);
        when(productMapper.toDto(any())).thenReturn(testResponseDTO);

        ResponseDTO result = productService.addProducte(testRequestDTO);
        assertNotNull(result);
        verify(productRepository).save(any());
    }

    @Test
    void addProducte_WhenProductNotExists_ShouldReturnNull() {
        when(productRepository.existsByNameAndSku(any(), any())).thenReturn(false);

        ResponseDTO result = productService.addProducte(testRequestDTO);
        assertNull(result);
        verify(productRepository, never()).save(any());
    }

    @Test
    void getAllProducts_Test() {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(testProduct));
        assertEquals(1, productService.getAllProducts().size());
    }

    @Test
    void getProductById_Test() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        assertTrue(productService.getProductById(1L).isPresent());
    }

    @Test
    void updateProduct_Test() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any())).thenReturn(testProduct);
        when(productMapper.toDto(any())).thenReturn(testResponseDTO);
        assertNotNull(productService.updateProduct(1L, testRequestDTO));
    }

    @Test
    void deleteMethods_Test() {
        productService.deleteProductById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void checkProductAvailabilityBySku_Test() {
        when(productRepository.findBySku("SKU-123")).thenReturn(Optional.of(testProduct));
        assertTrue(productService.checkProductAvailabilityBySku("SKU-123").isAvailable());

        when(productRepository.findBySku("INVALID")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> productService.checkProductAvailabilityBySku("INVALID"));
    }

    @Test
    void softDeleteProduct_Test() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        productService.softDeleteProduct(1L);
        assertTrue(testProduct.isDeleted());
    }

    // --- Action Active Tests ---

    @Test
    void actionActiveProduct_WhenInactive_ShouldActivate() {
        testProduct.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Boolean result = productService.actionActiveProduct(1L);

        assertTrue(result);
        assertTrue(testProduct.isActive());
        verify(productRepository).save(testProduct);
    }

    @Test
    void actionActiveProduct_WhenActive_NoOrders_NoStock_ShouldDeactivate() {
        testProduct.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        lenient().when(salesOrderService.checkStustOrderByProduct(testProduct)).thenReturn(false);
        lenient().when(inventoryService.chectQuentutProduct(testProduct)).thenReturn(false);

        Boolean result = productService.actionActiveProduct(1L);

        assertTrue(result);
        assertFalse(testProduct.isActive());
        verify(productRepository).save(testProduct);
    }

    @Test
    void actionActiveProduct_WhenActive_HasOrders_ShouldThrowException() {
        testProduct.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        when(salesOrderService.checkStustOrderByProduct(testProduct)).thenReturn(true);
        lenient().when(inventoryService.chectQuentutProduct(testProduct)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.actionActiveProduct(1L));

        assertTrue(ex.getMessage().contains("this product all ready in Order"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void actionActiveProduct_WhenActive_HasStock_ShouldThrowException() {
        testProduct.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        when(salesOrderService.checkStustOrderByProduct(testProduct)).thenReturn(false);
        when(inventoryService.chectQuentutProduct(testProduct)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.actionActiveProduct(1L));

        assertTrue(ex.getMessage().contains("this product all ready in Order"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void actionActiveProduct_NotFound_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.actionActiveProduct(1L));
    }
}