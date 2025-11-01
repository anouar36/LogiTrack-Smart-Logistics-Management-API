package com.logitrack.logitrack.dto.Inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestAddQtyOnHandDto {

    @NotNull(message = "quantityOnHand is required")
    @Min(value = 1, message = "quantityOnHand must be greater than 0")
    private Long quantityOnHand;

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "warehouseId is required")
    private int warehouseId;
}
