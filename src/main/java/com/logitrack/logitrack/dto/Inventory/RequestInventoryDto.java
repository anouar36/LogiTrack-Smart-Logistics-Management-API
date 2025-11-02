package com.logitrack.logitrack.dto.Inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
public class RequestInventoryDto {

    private Long id;

    @NotNull(message = "quantityOnHand is required")
    @Min(value = 0, message = "quantityOnHand cannot be negative")
    private Long quantityOnHand;

    @NotNull(message = "quantityReserved is required")
    @Min(value = 0, message = "quantityReserved cannot be negative")
    private Long quantityReserved;

    private LocalDateTime lastUpdatedAt = LocalDateTime.now();

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "warehouseId is required")
    private Long warehouseId;
}
