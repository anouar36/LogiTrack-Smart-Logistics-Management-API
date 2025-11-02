package com.logitrack.logitrack.dto.Inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseInventoryDto {

    private Long id;
    private Long quantityOnHand;
    private Long quantityReserved;
    private Instant lastUpdatedAt;

    private Long productId;
    private Long warehouseId;

    // Optional: you can add extra info for response if needed
    private String productName;
    private String warehouseName;
}
