package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse,Long> {
    
    Optional<Warehouse> findByCode(String code);
    
    boolean existsByCode(String code);
    
    List<Warehouse> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT w FROM Warehouse w LEFT JOIN FETCH w.inventories WHERE w.id = :id")
    Optional<Warehouse> findByIdWithInventories(@Param("id") Long id);
    
    @Query("SELECT COUNT(DISTINCT i.product.id) FROM Inventory i WHERE i.warehouse.id = :warehouseId")
    Long countProductsByWarehouseId(@Param("warehouseId") Long warehouseId);
    
    @Query("SELECT SUM(i.quantityOnHand) FROM Inventory i WHERE i.warehouse.id = :warehouseId")
    Long sumQuantityByWarehouseId(@Param("warehouseId") Long warehouseId);
}
