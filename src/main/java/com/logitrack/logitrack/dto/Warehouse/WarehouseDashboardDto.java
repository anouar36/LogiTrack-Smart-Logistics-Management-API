package com.logitrack.logitrack.dto.Warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Warehouse Dashboard Statistics
 * Used for displaying comprehensive warehouse analytics
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseDashboardDto {

    private Long totalWarehouses;
    private Long totalProducts;
    private Long totalInventoryValue;
    private Long totalStockQuantity;
    private Long lowStockItems;
    private Long outOfStockItems;
    
    private List<WarehouseStatsDto> warehouseStats;
    private List<TopWarehouseDto> topWarehousesByInventory;
    private List<LowStockAlertDto> lowStockAlerts;
}
