package com.logitrack.logitrack.mapper;

import com.logitrack.logitrack.dto.InventoryMovement.InventoryMovementRespenceDTO;
import com.logitrack.logitrack.entity.InventoryMovement;
import com.logitrack.logitrack.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class InventoryMovementMapper {

    public InventoryMovementRespenceDTO toDto(Product product, InventoryMovement movement) {
        InventoryMovementRespenceDTO dto = new InventoryMovementRespenceDTO();
        dto.setId(movement.getId());
        dto.setProduct(product);
        dto.setQuantity(movement.getQuantity());
        dto.setCreated_at(movement.getOccurredAt());
        dto.setQuantityOnHand(movement.getInventory().getQuantityOnHand());
        return dto;
    }

}
