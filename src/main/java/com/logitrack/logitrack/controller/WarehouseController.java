package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.Warehouse.*;
import com.logitrack.logitrack.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Warehouse Management
 * Handles all warehouse-related HTTP requests
 */
@RestController
@RequestMapping("/api/warehouses")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * Create a new warehouse
     * POST /api/warehouses
     */
    @PostMapping
    public ResponseEntity<WarehouseResponseDto> createWarehouse(@Valid @RequestBody WarehouseRequestDto requestDto) {
        WarehouseResponseDto response = warehouseService.createWarehouse(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all warehouses
     * GET /api/warehouses
     */
    @GetMapping
    public ResponseEntity<List<WarehouseResponseDto>> getAllWarehouses() {
        List<WarehouseResponseDto> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }

    /**
     * Get warehouse by ID
     * GET /api/warehouses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponseDto> getWarehouseById(@PathVariable Long id) {
        WarehouseResponseDto warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(warehouse);
    }

    /**
     * Get warehouse details with inventory information
     * GET /api/warehouses/{id}/details
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<WarehouseDetailDto> getWarehouseDetails(@PathVariable Long id) {
        WarehouseDetailDto details = warehouseService.getWarehouseDetails(id);
        return ResponseEntity.ok(details);
    }

    /**
     * Update warehouse
     * PUT /api/warehouses/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponseDto> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequestDto requestDto) {
        WarehouseResponseDto updatedWarehouse = warehouseService.updateWarehouse(id, requestDto);
        return ResponseEntity.ok(updatedWarehouse);
    }

    /**
     * Delete warehouse
     * DELETE /api/warehouses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search warehouses by name
     * GET /api/warehouses/search?name={name}
     */
    @GetMapping("/search")
    public ResponseEntity<List<WarehouseResponseDto>> searchWarehouses(@RequestParam String name) {
        List<WarehouseResponseDto> warehouses = warehouseService.searchWarehousesByName(name);
        return ResponseEntity.ok(warehouses);
    }

    /**
     * Get warehouse inventory for a specific product
     * GET /api/warehouses/{warehouseId}/products/{productId}/inventory
     */
    @GetMapping("/{warehouseId}/products/{productId}/inventory")
    public ResponseEntity<List<WarehouseInventoryDto>> getWarehouseInventoryForProduct(
            @PathVariable Long warehouseId,
            @PathVariable Long productId) {
        List<WarehouseInventoryDto> inventory = warehouseService.getWarehouseInventoryForProduct(warehouseId, productId);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get warehouse stock summary
     * GET /api/warehouses/{id}/summary
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<WarehouseResponseDto> getWarehouseStockSummary(@PathVariable Long id) {
        WarehouseResponseDto summary = warehouseService.getWarehouseStockSummary(id);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get comprehensive warehouse dashboard
     * GET /api/warehouses/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<WarehouseDashboardDto> getWarehouseDashboard() {
        WarehouseDashboardDto dashboard = warehouseService.getWarehouseDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get warehouse statistics
     * GET /api/warehouses/{id}/stats
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<WarehouseStatsDto> getWarehouseStats(@PathVariable Long id) {
        WarehouseStatsDto stats = warehouseService.getWarehouseStats(id);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get top warehouses by inventory
     * GET /api/warehouses/top?limit={limit}
     */
    @GetMapping("/top")
    public ResponseEntity<List<TopWarehouseDto>> getTopWarehouses(
            @RequestParam(defaultValue = "5") int limit) {
        List<TopWarehouseDto> topWarehouses = warehouseService.getTopWarehousesByInventory(limit);
        return ResponseEntity.ok(topWarehouses);
    }

    /**
     * Get low stock alerts
     * GET /api/warehouses/alerts/low-stock
     */
    @GetMapping("/alerts/low-stock")
    public ResponseEntity<List<LowStockAlertDto>> getLowStockAlerts() {
        List<LowStockAlertDto> alerts = warehouseService.getLowStockAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get warehouse inventory
     * GET /api/warehouses/{id}/inventory
     */
    @GetMapping("/{id}/inventory")
    public ResponseEntity<List<WarehouseInventoryDto>> getWarehouseInventory(@PathVariable Long id) {
        List<WarehouseInventoryDto> inventory = warehouseService.getWarehouseInventory(id);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Health check endpoint
     * GET /api/warehouses/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Warehouse Service is running");
    }
}
