package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    @Query("SELECT po FROM PurchaseOrder po " +
            "LEFT JOIN FETCH po.lines pol " +
            "LEFT JOIN FETCH pol.product " +
            "WHERE po.id = :poId")
    Optional<PurchaseOrder> findByIdWithLinesAndProducts(@Param("poId") Long poId);
}

