package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.AllocationDto;
import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.dto.Inventory.RequestInventoryDto;
import com.logitrack.logitrack.dto.Inventory.ResponseInventoryDto;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.MovementType;
import com.logitrack.logitrack.entity.enums.SOStatus;
import com.logitrack.logitrack.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class InventoryService {
    private final ModelMapper modelMapper;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMovementRepository movementRepository;
    private final  SalesOrderLineRepository salesOrderLineRepository;
    private final  SalesOrderRepository salesOrderRepository;




    //add Quantity OnHand
    public Inventory addQtyOnHand(RequestAddQtyOnHandDto dto) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(dto.getProductId(), dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Inventory does not exist,Please, can you create new Inventory?"));

        Long newQuantity = inventory.getQuantityOnHand() + dto.getQuantityOnHand();
        inventory.setQuantityOnHand(newQuantity);

        return inventoryRepository.save(inventory);
    }

    //create Inventory
    public ResponseInventoryDto creatInventory(RequestInventoryDto dto){
        Optional<Inventory> existingInventory =
                inventoryRepository.existsByProductIdAndWarehouseId(dto.getProductId(), dto.getWarehouseId());

        if (existingInventory.isPresent()) {
            throw new RuntimeException("Inventory already exists for this product in this warehouse");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Inventory inventory = modelMapper.map(dto,Inventory.class);
        return modelMapper.map(inventoryRepository.save(inventory), ResponseInventoryDto.class);


    }

    //reserve Product
    @Transactional
    public List<AllocationDto> reserveProduct(Long productId, Long quantityNeeded) {

        // list return of warehouseId and allocatedQuantity
        List<AllocationDto> allocations = new ArrayList<>();
        long remainingToReserve = quantityNeeded;

        //  find Available Stock For Product DESC
        List<Inventory> inventories = inventoryRepository.findAvailableStockForProduct(productId);

        if (inventories.isEmpty()) {
            throw new RuntimeException("No available stock found for product: " + productId);
        }

        // loop for all Inventory his have Quantity
        for (Inventory inv : inventories) {

            // if remainingToReserve <= 0 all new stock his reserved
            if (remainingToReserve <= 0) {
                break;
            }

            // catch Quantity his want this Inventory
            long availableInThisWarehouse = inv.getQuantityOnHand() - inv.getQuantityReserved();
            long qtyToReserveFromThis = Math.min(availableInThisWarehouse, remainingToReserve);

            //reserved this Quantity
            inv.setQuantityReserved(inv.getQuantityReserved() + qtyToReserveFromThis);
            inventoryRepository.save(inv);

            // add in List of this dto becouse return
            allocations.add(new AllocationDto(inv.getWarehouse().getId(), qtyToReserveFromThis));

            // Quantity available = Quantity available - Reserved quantity
            remainingToReserve -= qtyToReserveFromThis;
        }

        if (remainingToReserve > 0) {
            throw new RuntimeException(
                    "Unable to reserve the full quantity. Remaining unallocated quantity: " + remainingToReserve
            );
        }

        return allocations;
    }

    //receive Stock And Full fill Backorders
    @Transactional
    public void receiveStockAndFulfillBackorders(Product product, Warehouse warehouse, Long quantityReceived) {


        // Get Inventory by Product an warehouse
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductAndWarehouse(product, warehouse);

        // if this Inventory exists use this else create new Inventory

        Inventory inventory;
        if (inventoryOpt.isPresent()) {

            inventory = inventoryOpt.get();
        } else {
            inventory = Inventory.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .quantityOnHand(0L)
                    .quantityReserved(0L)
                    .movements(new ArrayList<>())
                    .build();
        }

        // Add new sotck to laste stock
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantityReceived);
        inventoryRepository.save(inventory);

        // create Movement for this change stock;
        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .inventory(inventory)
                .quantity(quantityReceived)
                .type(MovementType.INBOUND)
                .build();
        movementRepository.save(movement);


        // Get Available Stock After Add Stock QH - QR
        long availableStock = getGlobalAvailableStock(product.getId());

        // if availableStock <= 0 this his not desponible for backsOrders
        if (availableStock <= 0) {
            return;
        }

        //Get all OrdersLine his have avalue > 0 in column Remaining_Quantity_To_Reserve
        List<SalesOrderLine> linesToFulfill = salesOrderLineRepository.findBackordersForProduct(product.getId());

        // for chow orderLine all ready upate
        Set<Long> updatedOrderIds = new HashSet<>();

        //loop for all SalesOrderLine for change Remaining_Quantity_To_Reserve if availableStock > 0 else return for loop to anther product
        for (SalesOrderLine line : linesToFulfill) {
            if (availableStock <= 0) {
                break;
            }

            // Quantity his needed this OrderLin
            long needed = line.getRemainingQuantityToReserve();

            long canReserveNow = Math.min(availableStock, needed);

            // call function reserve Product for reserved Quentity Rolback Order
            reserveProduct(product.getId(), canReserveNow);

            // update Remaining_Quantity_To_Reserve if all quantity desponible Remaining_Quantity_To_Reserve=0
            line.setRemainingQuantityToReserve(needed - canReserveNow);
            salesOrderLineRepository.save(line);

            // We reduce the available stock.
            availableStock -= canReserveNow;
            updatedOrderIds.add(line.getSalesOrder().getId());
        }

        // call function checkAndSetOrderStatus for reflsh database salseOrder
        // if order all lins his have 0 in Remaining_Quantity_To_Reserve
        // and his have status created change status to reserved automatique
        for (Long orderId : updatedOrderIds) {
            checkAndSetOrderStatus(orderId);
        }
    }

    // calcul avilble sotck of som product  in inventory = totalOnHand - totalReserved
    public long getGlobalAvailableStock(Long productId) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        long totalOnHand = inventories.stream().mapToLong(Inventory::getQuantityOnHand).sum();
        long totalReserved = inventories.stream().mapToLong(Inventory::getQuantityReserved).sum();
        return totalOnHand - totalReserved;
    }

    // check Remaining_Quantity_To_Reserve And SetOrder Status
    private void checkAndSetOrderStatus(Long orderId) {
        SalesOrder order = salesOrderRepository.findByIdWithLinesAndProducts(orderId).orElse(null);
        if (order == null) return;

        // check if Remaining_Quantity_To_Reserve his hav value or not
        boolean allReserved = order.getLines().stream()
                .allMatch(line -> line.getRemainingQuantityToReserve() == 0);

        if (allReserved) {
            order.setStatus(SOStatus.RESERVED);
            salesOrderRepository.save(order);
        }
    }
    public boolean chectQuentutProduct(Product product){
        Inventory inventory = inventoryRepository.findByProduct(product);

        if (inventory == null) {
            return false;
        }
        if(inventory.getQuantityOnHand() > 0){
            return  false ;
        }else{
            return true ;
        }

    }


}


