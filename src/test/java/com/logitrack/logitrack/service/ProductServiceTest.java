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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    private SalesOrderLineService salesOrderLineService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private RequestDTO testRequestDTO;
    private ResponseDTO testResponseDTO;

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
                .build();        testRequestDTO = new RequestDTO();
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
    void addProduct_WhenProductExists_ShouldReturnResponseDTO() {
        // Given
        when(productRepository.existsByNameAndSku(testRequestDTO.getName(), testRequestDTO.getSku()))
                .thenReturn(true);
        when(productMapper.toEntity(testRequestDTO)).thenReturn(testProduct);
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDto(testProduct)).thenReturn(testResponseDTO);

        // When
        ResponseDTO result = productService.addProducte(testRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(testResponseDTO.getId(), result.getId());
        assertEquals(testResponseDTO.getSku(), result.getSku());
        assertEquals(testResponseDTO.getName(), result.getName());

        verify(productRepository).existsByNameAndSku(testRequestDTO.getName(), testRequestDTO.getSku());
        verify(productMapper).toEntity(testRequestDTO);
        verify(productRepository).save(testProduct);
        verify(productMapper).toDto(testProduct);
    }

    @Test
    void addProduct_WhenProductDoesNotExist_ShouldReturnNull() {
        // Given
        when(productRepository.existsByNameAndSku(testRequestDTO.getName(), testRequestDTO.getSku()))
                .thenReturn(false);

        // When
        ResponseDTO result = productService.addProducte(testRequestDTO);

        // Then
        assertNull(result);
        verify(productRepository).existsByNameAndSku(testRequestDTO.getName(), testRequestDTO.getSku());
        verify(productMapper, never()).toEntity(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // When
        List<Product> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct, result.get(0));
        verify(productRepository).findAll();
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        Optional<Product> result = productService.getProductById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProduct, result.get());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_WhenProductNotExists_ShouldReturnEmpty() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = productService.getProductById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(productRepository).findById(1L);
    }

    @Test
    void updateProduct_WhenProductExists_ShouldReturnUpdatedProduct() {
        // Given
        Product existingProduct = testProduct;        RequestDTO updateRequest = new RequestDTO();
        updateRequest.setName("Updated Product");
        updateRequest.setSku("UPDATED-SKU");
        updateRequest.setCategory("Updated Category");
        updateRequest.setPrice(BigDecimal.valueOf(149.99));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);
        when(productMapper.toDto(existingProduct)).thenReturn(testResponseDTO);

        // When
        ResponseDTO result = productService.updateProduct(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toDto(existingProduct);
    }

    @Test
    void updateProduct_WhenProductNotExists_ShouldReturnNull() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        ResponseDTO result = productService.updateProduct(1L, testRequestDTO);

        // Then
        assertNull(result);
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProductById_ShouldCallRepositoryDelete() {
        // When
        productService.deleteProductById(1L);

        // Then
        verify(productRepository).deleteById(1L);
    }

    @Test
    void productExistsById_WhenProductExists_ShouldReturnTrue() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = productService.productExistsById(1L);

        // Then
        assertTrue(result);
        verify(productRepository).existsById(1L);
    }

    @Test
    void productExistsById_WhenProductNotExists_ShouldReturnFalse() {
        // Given
        when(productRepository.existsById(1L)).thenReturn(false);

        // When
        boolean result = productService.productExistsById(1L);

        // Then
        assertFalse(result);
        verify(productRepository).existsById(1L);
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategory("Electronics")).thenReturn(products);

        // When
        List<Product> result = productService.getProductsByCategory("Electronics");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct, result.get(0));
        verify(productRepository).findByCategory("Electronics");
    }

    @Test
    void getProductsByPriceGreaterThan_ShouldReturnFilteredProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Double price = 50.0;
        when(productRepository.findByPriceGreaterThan(price)).thenReturn(products);

        // When
        List<Product> result = productService.getProductsByPriceGreaterThan(price);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByPriceGreaterThan(price);
    }

    @Test
    void getProductsByPriceBetween_ShouldReturnFilteredProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        Double minPrice = 50.0;
        Double maxPrice = 150.0;
        when(productRepository.findByPriceBetween(minPrice, maxPrice)).thenReturn(products);

        // When
        List<Product> result = productService.getProductsByPriceBetween(minPrice, maxPrice);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByPriceBetween(minPrice, maxPrice);
    }

    @Test
    void softDeleteProduct_WhenProductExists_ShouldMarkAsDeleted() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When
        productService.softDeleteProduct(1L);

        // Then
        assertTrue(testProduct.isDeleted());
        verify(productRepository).findById(1L);
        verify(productRepository).save(testProduct);
    }

    @Test
    void softDeleteProduct_WhenProductNotExists_ShouldNotThrowException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertDoesNotThrow(() -> productService.softDeleteProduct(1L));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void getAllActiveProducts_ShouldReturnOnlyActiveProducts() {
        // Given
        List<Product> activeProducts = Arrays.asList(testProduct);
        when(productRepository.findByDeletedFalse()).thenReturn(activeProducts);

        // When
        List<Product> result = productService.getAllActiveProducts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByDeletedFalse();
    }

    @Test
    void checkProductAvailabilityBySku_WhenProductExistsAndActive_ShouldReturnAvailable() {
        // Given
        when(productRepository.findBySku("TEST-SKU-001")).thenReturn(Optional.of(testProduct));

        // When
        ProductAvailabilityDto result = productService.checkProductAvailabilityBySku("TEST-SKU-001");

        // Then
        assertNotNull(result);
        assertEquals("TEST-SKU-001", result.getSku());
        assertTrue(result.isAvailable());
        assertEquals("Produit disponible à la vente.", result.getMessage());
        verify(productRepository).findBySku("TEST-SKU-001");
    }

    @Test
    void checkProductAvailabilityBySku_WhenProductExistsButInactive_ShouldReturnNotAvailable() {
        // Given
        Product inactiveProduct = testProduct;
        inactiveProduct.setActive(false);
        when(productRepository.findBySku("TEST-SKU-001")).thenReturn(Optional.of(inactiveProduct));

        // When
        ProductAvailabilityDto result = productService.checkProductAvailabilityBySku("TEST-SKU-001");

        // Then
        assertNotNull(result);
        assertEquals("TEST-SKU-001", result.getSku());
        assertFalse(result.getAvailable());
        assertEquals("Ce produit est inactif et n'est pas disponible à la vente.", result.getMessage());
        verify(productRepository).findBySku("TEST-SKU-001");
    }

    @Test
    void checkProductAvailabilityBySku_WhenProductNotExists_ShouldThrowException() {
        // Given
        when(productRepository.findBySku("INVALID-SKU")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> productService.checkProductAvailabilityBySku("INVALID-SKU"));
        
        assertEquals("Produit non trouvé avec SKU: INVALID-SKU", exception.getMessage());
        verify(productRepository).findBySku("INVALID-SKU");
    }

    @Test
    void actionActiveProduct_WhenProductIsActiveAndCanBeDeactivated_ShouldDeactivate() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(salesOrderService.checkStustOrderByProduct(testProduct)).thenReturn(false);
        when(inventoryService.chectQuentutProduct(testProduct)).thenReturn(false);

        // When
        Boolean result = productService.actionActiveProduct(1L);

        // Then
        assertTrue(result);
        assertFalse(testProduct.isActive());
        verify(productRepository).save(testProduct);
    }

    @Test
    void actionActiveProduct_WhenProductIsActiveButHasOrders_ShouldThrowException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(salesOrderService.checkStustOrderByProduct(testProduct)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> productService.actionActiveProduct(1L));
        
        assertEquals("this product all ready in Order created or reserved or his hav Quentity", 
            exception.getMessage());
    }

    @Test
    void actionActiveProduct_WhenProductIsInactiveAndCanBeActivated_ShouldActivate() {
        // Given
        Product inactiveProduct = testProduct;
        inactiveProduct.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(inactiveProduct));

        // When
        Boolean result = productService.actionActiveProduct(1L);

        // Then
        assertTrue(result);
        assertTrue(testProduct.isActive());
        verify(productRepository).save(testProduct);
    }

    @Test
    void actionActiveProduct_WhenProductNotExists_ShouldThrowException() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> productService.actionActiveProduct(1L));
        
        assertEquals("Shipment not found with id: 1", exception.getMessage());
    }

    @Test
    void countProducts_ShouldReturnProductCount() {
        // Given
        when(productRepository.count()).thenReturn(5L);

        // When
        long result = productService.countProducts();

        // Then
        assertEquals(5L, result);
        verify(productRepository).count();
    }

    @Test
    void getProductsByNameContaining_ShouldReturnMatchingProducts() {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByNameContaining("Test")).thenReturn(products);

        // When
        List<Product> result = productService.getProductsByNameContaining("Test");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByNameContaining("Test");
    }
}
