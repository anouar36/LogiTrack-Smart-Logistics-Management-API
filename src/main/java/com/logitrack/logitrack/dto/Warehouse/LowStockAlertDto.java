package com.logitrack.logitrack.dto.Warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for low stock alerts
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LowStockAlertDto {

    private Long warehouseId;
    private String warehouseName;
    private Long productId;
    private String productName;
    private String productSku;
    private Long currentQuantity;
    private Long reorderLevel;
    private String alertLevel; // "LOW", "CRITICAL", "OUT_OF_STOCK"
}
