package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Product.ProductDto;
import com.logitrack.logitrack.dto.Warehouse.*;
import com.logitrack.logitrack.entity.Inventory;
import com.logitrack.logitrack.entity.Warehouse;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.repository.InventoryRepository;
import com.logitrack.logitrack.repository.WarehouseRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Comparator;

@Service
@AllArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;

    /**
     * Create a new warehouse
     */
    @Transactional
    public WarehouseResponseDto createWarehouse(WarehouseRequestDto requestDto) {
        // Check if warehouse code already exists
        if (warehouseRepository.existsByCode(requestDto.getCode())) {
            throw new IllegalArgumentException("Warehouse with code '" + requestDto.getCode() + "' already exists");
        }

        Warehouse warehouse = Warehouse.builder()
                .code(requestDto.getCode())
                .name(requestDto.getName())
                .location(requestDto.getLocation())
                .description(requestDto.getDescription())
                .build();

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return mapToResponseDto(savedWarehouse);
    }

    /**
     * Get all warehouses
     */
    @Transactional(readOnly = true)
    public List<WarehouseResponseDto> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get warehouse by ID
     */
    @Transactional(readOnly = true)
    public WarehouseResponseDto getWarehouseById(Long id) {
        Warehouse warehouse = findWarehouseOrThrow(id);
        return mapToResponseDto(warehouse);
    }

    /**
     * Get warehouse details with inventory information
     */
    @Transactional(readOnly = true)
    public WarehouseDetailDto getWarehouseDetails(Long id) {
        Warehouse warehouse = warehouseRepository.findByIdWithInventories(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        List<WarehouseInventoryDto> inventoryDtos = warehouse.getInventories().stream()
                .map(this::mapToWarehouseInventoryDto)
                .collect(Collectors.toList());

        Long totalProducts = (long) warehouse.getInventories().size();
        Long totalQuantity = warehouse.getInventories().stream()
                .mapToLong(Inventory::getQuantityOnHand)
                .sum();

        return WarehouseDetailDto.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .totalProducts(totalProducts)
                .totalQuantity(totalQuantity)
                .inventories(inventoryDtos)
                .build();
    }

    /**
     * Update warehouse
     */
    @Transactional
    public WarehouseResponseDto updateWarehouse(Long id, WarehouseRequestDto requestDto) {
        Warehouse warehouse = findWarehouseOrThrow(id);

        // Check if new code conflicts with existing warehouse
        if (!warehouse.getCode().equals(requestDto.getCode()) 
                && warehouseRepository.existsByCode(requestDto.getCode())) {
            throw new IllegalArgumentException("Warehouse with code '" + requestDto.getCode() + "' already exists");
        }

        warehouse.setCode(requestDto.getCode());
        warehouse.setName(requestDto.getName());
        warehouse.setLocation(requestDto.getLocation());
        warehouse.setDescription(requestDto.getDescription());

        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        return mapToResponseDto(updatedWarehouse);
    }

    /**
     * Delete warehouse (soft delete or validation)
     */
    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = findWarehouseOrThrow(id);

        // Check if warehouse has inventory
        if (warehouse.getInventories() != null && !warehouse.getInventories().isEmpty()) {
            throw new IllegalStateException("Cannot delete warehouse with existing inventory. " +
                    "Please transfer or remove all inventory first.");
        }

        warehouseRepository.delete(warehouse);
    }

    /**
     * Get warehouses by name (search)
     */
    @Transactional(readOnly = true)
    public List<WarehouseResponseDto> searchWarehousesByName(String name) {
        return warehouseRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Get warehouse inventory for a specific product
     */
    @Transactional(readOnly = true)
    public List<WarehouseInventoryDto> getWarehouseInventoryForProduct(Long warehouseId, Long productId) {
        Warehouse warehouse = findWarehouseOrThrow(warehouseId);

        return warehouse.getInventories().stream()
                .filter(inv -> inv.getProduct().getId().equals(productId))
                .map(this::mapToWarehouseInventoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Get warehouse stock summary
     */
    @Transactional(readOnly = true)
    public WarehouseResponseDto getWarehouseStockSummary(Long id) {
        Warehouse warehouse = findWarehouseOrThrow(id);

        Long totalProducts = warehouseRepository.countProductsByWarehouseId(id);
        Long totalQuantity = warehouseRepository.sumQuantityByWarehouseId(id);

        return WarehouseResponseDto.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .totalProducts(totalProducts != null ? totalProducts : 0L)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0L)
                .build();
    }

    /**
     * Get comprehensive warehouse dashboard statistics
     */
    @Transactional(readOnly = true)
    public WarehouseDashboardDto getWarehouseDashboard() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        
        Long totalWarehouses = (long) warehouses.size();
        Long totalProducts = 0L;
        Long totalStockQuantity = 0L;
        Long lowStockItems = 0L;
        Long outOfStockItems = 0L;
        
        List<WarehouseStatsDto> warehouseStats = warehouses.stream()
                .map(this::mapToWarehouseStatsDto)
                .collect(Collectors.toList());
        
        // Calculate totals
        for (WarehouseStatsDto stats : warehouseStats) {
            totalProducts += stats.getProductCount();
            totalStockQuantity += stats.getTotalQuantity();
        }
        
        // Get low stock alerts
        List<LowStockAlertDto> lowStockAlerts = getLowStockAlerts();
        lowStockItems = lowStockAlerts.stream()
                .filter(alert -> "LOW".equals(alert.getAlertLevel()) || "CRITICAL".equals(alert.getAlertLevel()))
                .count();
        outOfStockItems = lowStockAlerts.stream()
                .filter(alert -> "OUT_OF_STOCK".equals(alert.getAlertLevel()))
                .count();
        
        // Get top warehouses
        List<TopWarehouseDto> topWarehouses = getTopWarehousesByInventory(5);
        
        return WarehouseDashboardDto.builder()
                .totalWarehouses(totalWarehouses)
                .totalProducts(totalProducts)
                .totalStockQuantity(totalStockQuantity)
                .totalInventoryValue(0L) // Can be calculated if product prices are available
                .lowStockItems(lowStockItems)
                .outOfStockItems(outOfStockItems)
                .warehouseStats(warehouseStats)
                .topWarehousesByInventory(topWarehouses)
                .lowStockAlerts(lowStockAlerts)
                .build();
    }

    /**
     * Get warehouse statistics by ID
     */
    @Transactional(readOnly = true)
    public WarehouseStatsDto getWarehouseStats(Long warehouseId) {
        Warehouse warehouse = findWarehouseOrThrow(warehouseId);
        return mapToWarehouseStatsDto(warehouse);
    }

    /**
     * Get top warehouses by inventory quantity
     */
    @Transactional(readOnly = true)
    public List<TopWarehouseDto> getTopWarehousesByInventory(int limit) {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        
        AtomicInteger rank = new AtomicInteger(1);
        
        return warehouses.stream()
                .map(warehouse -> {
                    Long productCount = warehouseRepository.countProductsByWarehouseId(warehouse.getId());
                    Long totalQuantity = warehouseRepository.sumQuantityByWarehouseId(warehouse.getId());
                    
                    return TopWarehouseDto.builder()
                            .warehouseId(warehouse.getId())
                            .warehouseName(warehouse.getName())
                            .warehouseCode(warehouse.getCode())
                            .totalProducts(productCount != null ? productCount : 0L)
                            .totalQuantity(totalQuantity != null ? totalQuantity : 0L)
                            .build();
                })
                .sorted(Comparator.comparing(TopWarehouseDto::getTotalQuantity).reversed())
                .limit(limit)
                .map(dto -> {
                    dto.setRank(rank.getAndIncrement());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get low stock alerts across all warehouses
     */
    @Transactional(readOnly = true)
    public List<LowStockAlertDto> getLowStockAlerts() {
        List<Inventory> inventories = inventoryRepository.findAll();
        
        return inventories.stream()
                .map(inventory -> {
                    Long currentQuantity = inventory.getQuantityOnHand() - inventory.getQuantityReserved();
                    Long reorderLevel = 10L; // Default reorder level, should come from product settings
                    
                    String alertLevel;
                    if (currentQuantity == 0) {
                        alertLevel = "OUT_OF_STOCK";
                    } else if (currentQuantity <= reorderLevel / 2) {
                        alertLevel = "CRITICAL";
                    } else if (currentQuantity <= reorderLevel) {
                        alertLevel = "LOW";
                    } else {
                        return null; // Not a low stock item
                    }
                    
                    return LowStockAlertDto.builder()
                            .warehouseId(inventory.getWarehouse().getId())
                            .warehouseName(inventory.getWarehouse().getName())
                            .productId(inventory.getProduct().getId())
                            .productName(inventory.getProduct().getName())
                            .productSku(inventory.getProduct().getSku())
                            .currentQuantity(currentQuantity)
                            .reorderLevel(reorderLevel)
                            .alertLevel(alertLevel)
                            .build();
                })
                .filter(alert -> alert != null)
                .sorted(Comparator.comparing(LowStockAlertDto::getCurrentQuantity))
                .collect(Collectors.toList());
    }

    /**
     * Get inventory movements for a warehouse (last 30 days)
     */
    @Transactional(readOnly = true)
    public List<WarehouseInventoryDto> getWarehouseInventory(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findByIdWithInventories(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));
        
        return warehouse.getInventories().stream()
                .map(this::mapToWarehouseInventoryDto)
                .collect(Collectors.toList());
    }

    // ============= Helper Methods =============

    private Warehouse findWarehouseOrThrow(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    private WarehouseResponseDto mapToResponseDto(Warehouse warehouse) {
        Long totalProducts = warehouseRepository.countProductsByWarehouseId(warehouse.getId());
        Long totalQuantity = warehouseRepository.sumQuantityByWarehouseId(warehouse.getId());

        return WarehouseResponseDto.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .totalProducts(totalProducts != null ? totalProducts : 0L)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0L)
                .build();
    }

    private WarehouseInventoryDto mapToWarehouseInventoryDto(Inventory inventory) {
        ProductDto productDto = modelMapper.map(inventory.getProduct(), ProductDto.class);
        
        Long availableQuantity = inventory.getQuantityOnHand() - inventory.getQuantityReserved();

        return WarehouseInventoryDto.builder()
                .inventoryId(inventory.getId())
                .product(productDto)
                .quantityOnHand(inventory.getQuantityOnHand())
                .quantityReserved(inventory.getQuantityReserved())
                .availableQuantity(availableQuantity)
                .build();
    }

    private WarehouseStatsDto mapToWarehouseStatsDto(Warehouse warehouse) {
        Long productCount = warehouseRepository.countProductsByWarehouseId(warehouse.getId());
        Long totalQuantity = warehouseRepository.sumQuantityByWarehouseId(warehouse.getId());
        
        // Calculate available and reserved quantities
        Long availableQuantity = 0L;
        Long reservedQuantity = 0L;
        
        if (warehouse.getInventories() != null) {
            for (Inventory inv : warehouse.getInventories()) {
                availableQuantity += (inv.getQuantityOnHand() - inv.getQuantityReserved());
                reservedQuantity += inv.getQuantityReserved();
            }
        }
        
        // Calculate utilization (example: based on max capacity of 10000 units)
        Long maxCapacity = 10000L;
        Double utilization = totalQuantity != null ? (totalQuantity.doubleValue() / maxCapacity) * 100 : 0.0;
        
        return WarehouseStatsDto.builder()
                .warehouseId(warehouse.getId())
                .warehouseName(warehouse.getName())
                .warehouseCode(warehouse.getCode())
                .location(warehouse.getLocation())
                .productCount(productCount != null ? productCount : 0L)
                .totalQuantity(totalQuantity != null ? totalQuantity : 0L)
                .availableQuantity(availableQuantity)
                .reservedQuantity(reservedQuantity)
                .utilizationPercentage(utilization)
                .build();
    }
}
