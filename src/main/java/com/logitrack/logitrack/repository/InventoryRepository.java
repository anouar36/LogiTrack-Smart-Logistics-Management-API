package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Inventory;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, int warehouseId);
    Optional<Inventory> existsByProductIdAndWarehouseId (Long productId ,Long warehouseId );
    Optional<Inventory>  findByProductAndWarehouse(Product product, Warehouse warehouse);
    List<Inventory> findByProductId(Long productId);

    @Query("SELECT i FROM Inventory i " +
            "WHERE i.product.id = :productId " +
            "AND i.quantityOnHand > i.quantityReserved " + // الشرط: الستوك المتاح > 0
            "ORDER BY (i.quantityOnHand - i.quantityReserved) DESC") // الترتيب: من الكبير للصغير
    List<Inventory> findAvailableStockForProduct(@Param("productId") Long productId);




}
