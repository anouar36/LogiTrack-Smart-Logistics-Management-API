package com.logitrack.logitrack.dto.InventoryMovement;

import com.logitrack.logitrack.entity.Product;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryMovementRespenceDTO {

    private int id;
    private Product product;
    private Long quantity;
    private Long quantityOnHand; // الكمية بعد التحديث
    private LocalDateTime created_at;
}
