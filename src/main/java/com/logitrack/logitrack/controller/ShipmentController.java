package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.Shipment.ShipmentDTO;
import com.logitrack.logitrack.service.ShipmentService; // Ghadi nssawboh daba
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Autowired
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }


    @PostMapping("/{id}/dispatch")
    public ResponseEntity<ShipmentDTO> dispatchShipment(@PathVariable Long id) {
        ShipmentDTO updatedShipment = shipmentService.dispatchShipment(id);
        return ResponseEntity.ok(updatedShipment);
    }


    @PostMapping("/{id}/deliver")
    public ResponseEntity<ShipmentDTO> deliverShipment(@PathVariable Long id) {
        ShipmentDTO updatedShipment = shipmentService.deliverShipment(id);
        return ResponseEntity.ok(updatedShipment);
    }
}