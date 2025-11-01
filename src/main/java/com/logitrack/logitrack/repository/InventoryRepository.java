package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, int warehouseId);

}
