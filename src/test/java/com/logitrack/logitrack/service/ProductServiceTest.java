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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private ResponseDTO testResponseDTO;    @BeforeEach
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

        testRequestDTO = new RequestDTO();
        testRequestDTO.setSku("TEST-SKU-001");
        testRequestDTO.setName("Test Product");
        testRequestDTO.setCategory("Electronics");
        testRequestDTO.setPrice(BigDecimal.valueOf(99.99));

        testResponseDTO = new ResponseDTO();
        testResponseDTO.setId(1L);
        testResponseDTO.setSku("TEST-SKU-001");
        testResponseDTO.setName("Test Product");
        testResponseDTO.setCategory("Electronics");
        testResponseDTO.setPrice(BigDecimal.valueOf(99.99));
    }

    @Test
    void addProducte_WhenProductExists_ShouldReturnResponseDTO() {
        when(productRepository.existsByNameAndSku(any(), any())).thenReturn(true);
        when(productMapper.toEntity(any())).thenReturn(testProduct);
        when(productRepository.save(any())).thenReturn(testProduct);
        when(productMapper.toDto(any())).thenReturn(testResponseDTO);

        ResponseDTO result = productService.addProducte(testRequestDTO);

        assertNotNull(result);
    }

    @Test
    void actionActiveProduct_WhenProductIsActiveAndCanBeDeactivated_ShouldDeactivate() {
        // Given
        testProduct.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Using lenient() solves unnecessary stubbing issue
        lenient().when(salesOrderService.checkStustOrderByProduct(testProduct)).thenReturn(false);
        lenient().when(inventoryService.chectQuentutProduct(testProduct)).thenReturn(false);

        // When
        Boolean result = productService.actionActiveProduct(1L);

        // Then
        assertTrue(result);
        assertFalse(testProduct.isActive());
        verify(productRepository).save(testProduct);
    }    @Test
    void actionActiveProduct_WhenProductIsActiveButHasOrders_ShouldThrowException() {
        // Given
        testProduct.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(salesOrderService.checkStustOrderByProduct(testProduct)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> productService.actionActiveProduct(1L));
        assertEquals(
                "this product all ready in Order created or reserved or his hav Quentity",
                exception.getMessage()
        );
    }

    // Add the rest of your CRUD tests here (get, update, delete…)
}
