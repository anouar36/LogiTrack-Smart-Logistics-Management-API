package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine,Long> {
}
