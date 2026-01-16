package com.logitrack.logitrack.dto.Warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for top performing warehouses
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopWarehouseDto {

    private Long warehouseId;
    private String warehouseName;
    private String warehouseCode;
    private Long totalProducts;
    private Long totalQuantity;
    private Integer rank;
}
