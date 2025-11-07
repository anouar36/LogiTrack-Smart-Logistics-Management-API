package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine , Long> {
}
