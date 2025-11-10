package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Shipment.ShipmentDTO;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.MovementType;
import com.logitrack.logitrack.entity.enums.SOStatus;
import com.logitrack.logitrack.entity.enums.ShipmentStatus;
import com.logitrack.logitrack.repository.*;
import com.logitrack.logitrack.exception.BusinessException;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.exception.StockUnavailableException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Essential for data integrity

import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    public ShipmentService(ShipmentRepository shipmentRepository,
                           SalesOrderRepository salesOrderRepository,
                           InventoryRepository inventoryRepository,
                           InventoryMovementRepository inventoryMovementRepository) {
        this.shipmentRepository = shipmentRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
    }

    /**
     * US11 - Action 1: Handles the start of the shipment (Dispatch).
     * Transitions status from RESERVED -> SHIPPED.
     */
    @Transactional // Ensures that if any part fails, the database rolls back.
    public ShipmentDTO dispatchShipment(Long shipmentId) {

        // 1. Fetch the Shipment from the database
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + shipmentId));

        // 2. Fetch the related SalesOrder
        SalesOrder salesOrder = shipment.getSalesOrder();

        // 3. --- CRITICAL FIX ---
        // Add a null-check for the SalesOrder to prevent NullPointerException
        if (salesOrder == null) {
            throw new BusinessException("Shipment " + shipmentId + " is corrupt. It is not linked to any SalesOrder.");
        }

        // 4. Business Rule Validations (as per US11)
        if (shipment.getStatus() != ShipmentStatus.PLANNED) {
            throw new BusinessException("Shipment cannot be dispatched. Status is not PLANNED.");
        }
        if (salesOrder.getStatus() != SOStatus.RESERVED) {
            throw new BusinessException("SalesOrder cannot be shipped. Status is not RESERVED.");
        }

        // 5. Get the source Warehouse from the SalesOrder
        Warehouse warehouse = salesOrder.getWarehouse();
        if (warehouse == null) {
            // This is another potential null check
            throw new BusinessException("SalesOrder " + salesOrder.getId() + " is not linked to a source Warehouse.");
        }

        // 6. Loop through each line item in the order to update stock
        for (SalesOrderLine line : salesOrder.getLines()) {
            Product product = line.getProduct();
            long quantityToShip = line.getQuantity();

            // 7. Fetch the specific inventory record for this product and warehouse
            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                    .orElseThrow(() -> new BusinessException("Inventory not found for product: " + product.getSku()));

            // 8. Concurrency Check (from US11 Acceptance Criteria)
            // Check if the reserved quantity is still available
            if (inventory.getQuantityReserved() < quantityToShip) {
                throw new StockUnavailableException("Concurrency conflict. Reserved stock is less than required.");
            }

            // 9. Update Stock (The core logic)
            // US11: "qtyReserved decreases". US7 (OUTBOUND): "qtyOnHand decreases".
            inventory.setQuantityReserved(inventory.getQuantityReserved() - quantityToShip);
            inventory.setQuantityOnHand(inventory.getQuantityOnHand() - quantityToShip);
            inventoryRepository.save(inventory);

            // 10. Log the Inventory Movement (OUTBOUND)
            InventoryMovement movement = new InventoryMovement();
            movement.setProduct(product);

            // This links the movement to the specific inventory record (Product + Warehouse)
            movement.setInventory(inventory);

            movement.setType(MovementType.OUTBOUND);
            movement.setQuantity(quantityToShip);
            movement.setOccurredAt(LocalDateTime.now()); // Using LocalDateTime as per your entity
            movement.setReferenceDoc("SHIP-" + shipment.getId());
            inventoryMovementRepository.save(movement);
        }

        // 11. Update Statuses as per US11 requirements
        salesOrder.setStatus(SOStatus.SHIPPED); // Order is now SHIPPED
        shipment.setStatus(ShipmentStatus.IN_TRANSIT); // Shipment is now IN_TRANSIT
        shipment.setShippedAt(Instant.now()); // Record the time of shipment

        // 12. Save all changes to the database
        salesOrderRepository.save(salesOrder);
        Shipment savedShipment = shipmentRepository.save(shipment);

        // 13. Return the updated DTO to the client
        return toDto(savedShipment);
    }

    /**
     * US11 - Action 2: Handles the confirmation of delivery.
     * Transitions status from SHIPPED -> DELIVERED.
     */
    @Transactional
    public ShipmentDTO deliverShipment(Long shipmentId) {

        // 1. Fetch the Shipment and its related Order
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + shipmentId));

        SalesOrder salesOrder = shipment.getSalesOrder();

        // 2. --- CRITICAL FIX ---
        // Add a null-check for the SalesOrder
        if (salesOrder == null) {
            throw new BusinessException("Shipment " + shipmentId + " is corrupt. It is not linked to any SalesOrder.");
        }

        // 3. Business Rule Validations
        if (shipment.getStatus() != ShipmentStatus.IN_TRANSIT) {
            throw new BusinessException("Shipment cannot be delivered. Status is not IN_TRANSIT.");
        }

        // This check is good practice, though US11 doesn't explicitly require it
        if (salesOrder.getStatus() != SOStatus.SHIPPED) {
            // This could happen if a separate process changed the order
            throw new BusinessException("SalesOrder status is not SHIPPED. Cannot deliver.");
        }

        // 4. Update Statuses as per US11 requirements
        shipment.setStatus(ShipmentStatus.DELIVERED);
        salesOrder.setStatus(SOStatus.DELIVERED);
        shipment.setDeliveredAt(Instant.now()); // Record the time of delivery

        // 5. Save changes
        salesOrderRepository.save(salesOrder);
        Shipment savedShipment = shipmentRepository.save(shipment);

        // 6. Return the updated DTO
        return toDto(savedShipment);
    }


    // --- SIMPLE MANUAL MAPPER ---
    // This private method converts an Entity to a DTO

    private ShipmentDTO toDto(Shipment shipment) {
        if (shipment == null) {
            return null;
        }

        ShipmentDTO dto = new ShipmentDTO();
        dto.setId(shipment.getId());
        dto.setStatus(shipment.getStatus());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setShippedAt(shipment.getShippedAt());
        dto.setDeliveredAt(shipment.getDeliveredAt());

        // Convert complex objects (Entity) to simple IDs (Long) for the DTO
        if (shipment.getSalesOrder() != null) {
            dto.setSalesOrderId(shipment.getSalesOrder().getId());
        }
        if (shipment.getCarrier() != null) {
            dto.setCarrierId(shipment.getCarrier().getId());
        }

        return dto;
    }
}