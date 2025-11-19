package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.dto.InventoryMovement.InventoryMovementRequestDTO;
import com.logitrack.logitrack.dto.InventoryMovement.InventoryMovementRespenceDTO;
import com.logitrack.logitrack.entity.Inventory;
import com.logitrack.logitrack.entity.InventoryMovement;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.entity.enums.MovementType;
import com.logitrack.logitrack.exception.ProductNotExistsException;
import com.logitrack.logitrack.mapper.InventoryMapper;
import com.logitrack.logitrack.mapper.InventoryMovementMapper;
import com.logitrack.logitrack.repository.InventoryMovementRepository;
import com.logitrack.logitrack.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private InventoryMovementMapper inventoryMovementMapper;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryMovementService inventoryMovementService;

    // Test Data
    private Product mockProduct;
    private Inventory mockInventory;
    private InventoryMovementRequestDTO requestDto;
    private RequestAddQtyOnHandDto addQtyDto;
    private InventoryMovement savedMovement;
    private InventoryMovementRespenceDTO responseDto;

    @BeforeEach
    void setUp() {
        // 1. Setup Product
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Test Widget");

        // 2. Setup Inventory
        mockInventory = new Inventory();
        mockInventory.setId(100L);
        mockInventory.setProduct(mockProduct);

        // 3. Setup Request DTO
        requestDto = new InventoryMovementRequestDTO();
        requestDto.setIdProduc(1L);
        requestDto.setQuantity(50L);
        requestDto.setType(MovementType.INBOUND);
        requestDto.setReferenceDoc("DOC-001");

        // 4. Setup Intermediate DTO (Mapped from Request)
        addQtyDto = new RequestAddQtyOnHandDto();

        // 5. Setup Saved Entity (Mock result from repo)
        savedMovement = InventoryMovement.builder()
                .id(500)
                .product(mockProduct)
                .quantity(50L)
                .type(MovementType.INBOUND)
                .build();

        // 6. Setup Response DTO
        responseDto = new InventoryMovementRespenceDTO();
        responseDto.setId(500);
        responseDto.setQuantity(50L);
    }

    @Test
    @DisplayName("Add Movement: Success Scenario")
    void addInventoryMovement_Success() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(inventoryMapper.toAddQtyOnHandDto(requestDto)).thenReturn(addQtyDto);
        when(inventoryService.addQtyOnHand(addQtyDto)).thenReturn(mockInventory);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(savedMovement);
        when(inventoryMovementMapper.toDto(eq(mockProduct), any(InventoryMovement.class))).thenReturn(responseDto);

        // When
        InventoryMovementRespenceDTO result = inventoryMovementService.addInventoryMovement(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(500L, result.getId());

        // Verify interactions
        verify(productRepository).findById(1L);
        verify(inventoryService).addQtyOnHand(addQtyDto);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Add Movement: Product Not Found throws Exception")
    void addInventoryMovement_ProductNotFound() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotExistsException.class,
                () -> inventoryMovementService.addInventoryMovement(requestDto));

        // Verify we didn't proceed to update inventory or save movement
        verify(inventoryService, never()).addQtyOnHand(any());
        verify(inventoryMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add Movement: Quantity <= 0 throws IllegalArgumentException")
    void addInventoryMovement_InvalidQuantity() {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        requestDto.setQuantity(0L); // Invalid

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> inventoryMovementService.addInventoryMovement(requestDto));

        // Verify strict check for negative numbers too
        requestDto.setQuantity(-5L);
        assertThrows(IllegalArgumentException.class,
                () -> inventoryMovementService.addInventoryMovement(requestDto));
    }

    @Test
    @DisplayName("Add Movement: Null Type defaults to INBOUND")
    void addInventoryMovement_NullType_DefaultsToInbound() {
        // Given
        requestDto.setType(null); // Set type to null to test default logic

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));
        when(inventoryMapper.toAddQtyOnHandDto(requestDto)).thenReturn(addQtyDto);
        when(inventoryService.addQtyOnHand(addQtyDto)).thenReturn(mockInventory);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(savedMovement);
        when(inventoryMovementMapper.toDto(any(), any())).thenReturn(responseDto);

        // When
        inventoryMovementService.addInventoryMovement(requestDto);

        // Then - Capture the argument passed to save() to check the logic inside the service
        ArgumentCaptor<InventoryMovement> captor = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(inventoryMovementRepository).save(captor.capture());

        InventoryMovement capturedMovement = captor.getValue();
        assertEquals(MovementType.INBOUND, capturedMovement.getType(), "Should default to INBOUND if null");
        assertNotNull(capturedMovement.getOccurredAt(), "Timestamp should be set");
    }
}