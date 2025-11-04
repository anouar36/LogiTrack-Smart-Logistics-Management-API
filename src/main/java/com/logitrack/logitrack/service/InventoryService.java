package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.AllocationDto;
import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.dto.Inventory.RequestInventoryDto;
import com.logitrack.logitrack.dto.Inventory.ResponseInventoryDto;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.repository.InventoryRepository;
import com.logitrack.logitrack.repository.ProductRepository;
import com.logitrack.logitrack.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class InventoryService {
    private final ModelMapper modelMapper;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;




    public Inventory addQtyOnHand(RequestAddQtyOnHandDto dto) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(dto.getProductId(), dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Inventory does not exist,Please, can you create new Inventory?"));

        Long newQuantity = inventory.getQuantityOnHand() + dto.getQuantityOnHand();
        inventory.setQuantityOnHand(newQuantity);

        return inventoryRepository.save(inventory);
    }
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
    @Transactional
    public List<AllocationDto> reserveProduct(Long productId, Long requestedQuantity) {
        //get all inventories
        List<Inventory> inventories = inventoryRepository.findAll();

        //this for result Dto
        List<AllocationDto> result = new ArrayList<>();

        //this for Quantity demmend client
        Long remaining = requestedQuantity;

        //chechk if all inventories by wherhouse if his have the product or not for catch his Quentity
        for (Inventory inv : inventories) {
            if (!inv.getProduct().getId().equals(productId) || remaining <= 0) continue;

            Long available = inv.getQuantityOnHand() - inv.getQuantityReserved();

            if (available <= 0) continue;

            Long toReserve = Math.min(available, remaining);

            // UPDATE Quantity Reserved
            inv.setQuantityReserved(inv.getQuantityReserved() + toReserve);

            // add wharhouse and hou many quntity we catching
            result.add(new AllocationDto(inv.getWarehouse().getId(), toReserve));

            remaining -= toReserve;
        }
        if (remaining > 0) {
            System.out.println("We were unable to allocate the entire quantity requested; the remaining amount is:" + remaining);
        }

        return result;
    }

}
