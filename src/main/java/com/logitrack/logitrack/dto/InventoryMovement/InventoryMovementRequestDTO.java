package com.logitrack.logitrack.dto.InventoryMovement;

import com.logitrack.logitrack.entity.enums.MovementType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryMovementRequestDTO {

    @NotNull(message = "Product ID cannot be null")
    @Min(value = 1, message = "Product ID must be greater than 0")
    private Long idProduc;

    @NotNull(message = "Warehouse ID cannot be null")
    @Min(value = 1, message = "Warehouse ID must be greater than 0")
    private Integer idWarehouse;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotBlank(message = "Reference document cannot be blank")
    private String referenceDoc;

    @Enumerated(EnumType.STRING)
    private MovementType type;

}
