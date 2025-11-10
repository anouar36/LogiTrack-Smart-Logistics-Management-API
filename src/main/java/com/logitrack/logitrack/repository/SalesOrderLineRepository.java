package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.entity.SalesOrderLine;
import com.logitrack.logitrack.entity.enums.SOStatus;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
    @Query("SELECT sol FROM SalesOrderLine sol " +
            "WHERE sol.product.id = :productId " +
            "AND sol.remainingQuantityToReserve > 0 " +
            "AND sol.salesOrder.status = 'CREATED' " +
            "ORDER BY sol.salesOrder.createdAt ASC")
    List<SalesOrderLine> findBackordersForProduct(@Param("productId") Long productId);


    boolean existsByProductAndSalesOrder_StatusIn(Product product, List<SOStatus> statuses);

}
