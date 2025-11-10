package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment , Long > {
}
