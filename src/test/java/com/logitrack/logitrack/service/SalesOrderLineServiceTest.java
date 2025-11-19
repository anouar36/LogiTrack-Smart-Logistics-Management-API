package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.SalesOrder.ResponseSalesOrderLineDto;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.entity.SalesOrderLine;
import com.logitrack.logitrack.repository.SalesOrderLineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderLineServiceTest {

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

    @Mock
    private ModelMapper modelMapper; // Injected but not used in addOrderLine method logic

    @InjectMocks
    private SalesOrderLineService salesOrderLineService;

    private SalesOrderLine inputLine;
    private SalesOrderLine savedLine;
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        // 1. Setup Mock Product
        mockProduct = Product.builder()
                .id(100L)
                .sku("PROD-001")
                .name("Gaming Mouse")
                .category("Electronics")
                .price(BigDecimal.valueOf(50.00))
                .active(true)
                .deleted(false)
                .build();

        // 2. Setup Input Line (Before Save)
        inputLine = SalesOrderLine.builder()
                .product(mockProduct)
                .quantity(2L)
                .unitPrice(BigDecimal.valueOf(50.00))
                .totalPrice(BigDecimal.valueOf(100.00))
                .build();

        // 3. Setup Saved Line (After Save - has ID)
        savedLine = SalesOrderLine.builder()
                .id(1L)
                .product(mockProduct)
                .quantity(2L)
                .unitPrice(BigDecimal.valueOf(50.00))
                .totalPrice(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    @DisplayName("addOrderLine: Should save line and map to DTO correctly")
    void addOrderLine_Success() {
        // Given
        when(salesOrderLineRepository.save(inputLine)).thenReturn(savedLine);

        // When
        ResponseSalesOrderLineDto result = salesOrderLineService.addOrderLine(inputLine);

        // Then
        assertNotNull(result);

        // Verify Line Details
        assertEquals(savedLine.getId(), result.getId());
        assertEquals(savedLine.getQuantity(), result.getQuantity());
        assertEquals(savedLine.getUnitPrice(), result.getUnitPrice());
        assertEquals(savedLine.getTotalPrice(), result.getTotalPrice());

        // Verify Nested Product DTO Mapping
        assertNotNull(result.getProduct());
        assertEquals(mockProduct.getId(), result.getProduct().getId());
        assertEquals(mockProduct.getSku(), result.getProduct().getSku());
        assertEquals(mockProduct.getName(), result.getProduct().getName());
        assertEquals(mockProduct.getPrice(), result.getProduct().getPrice());
        assertTrue(result.getProduct().getActive());

        // Verify Repository Call
        verify(salesOrderLineRepository).save(inputLine);
    }
}