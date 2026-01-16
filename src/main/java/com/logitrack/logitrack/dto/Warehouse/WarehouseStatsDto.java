package com.logitrack.logitrack.dto.Warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for individual warehouse statistics
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseStatsDto {

    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private String location;
    private Long productCount;
    private Long totalQuantity;
    private Long availableQuantity;
    private Long reservedQuantity;
    private Double utilizationPercentage;
}
