package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.dto.Inventory.RequestInventoryDto;
import com.logitrack.logitrack.dto.Inventory.ResponseInventoryDto;
import com.logitrack.logitrack.entity.Inventory;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.entity.Warehouse;
import com.logitrack.logitrack.repository.InventoryRepository;
import com.logitrack.logitrack.repository.ProductRepository;
import com.logitrack.logitrack.repository.WarehouseRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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


}
