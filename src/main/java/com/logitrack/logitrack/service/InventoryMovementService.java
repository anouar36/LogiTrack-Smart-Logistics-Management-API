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
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class InventoryMovementService {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMovementMapper inventoryMovementMapper;
    private final  InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;


    @Transactional
    public InventoryMovementRespenceDTO addInventoryMovement(InventoryMovementRequestDTO dto) {

        Product product = productRepository.findById(dto.getIdProduc())
                .orElseThrow(() -> new ProductNotExistsException("This product does not exist"));

        if (dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        RequestAddQtyOnHandDto addQtyDto = inventoryMapper.toAddQtyOnHandDto(dto);

        Inventory updatedInventory = inventoryService.addQtyOnHand(addQtyDto);

        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .inventory(updatedInventory)
                .quantity(dto.getQuantity())
                .type(dto.getType() != null ? dto.getType() : MovementType.INBOUND)
                .referenceDoc(dto.getReferenceDoc())
                .occurredAt(java.time.LocalDateTime.now())
                .build();

        InventoryMovement savedMovement = inventoryMovementRepository.save(movement);

        return inventoryMovementMapper.toDto(product, savedMovement);
    }

}
