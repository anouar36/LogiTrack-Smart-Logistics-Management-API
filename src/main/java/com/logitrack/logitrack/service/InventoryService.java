package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.entity.Inventory;
import com.logitrack.logitrack.repository.InventoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {
    private final ModelMapper modelMapper;
    private final InventoryRepository inventoryRepository;


    public InventoryService(ModelMapper modelMapper, InventoryRepository inventoryRepository) {
        this.modelMapper = modelMapper;
        this.inventoryRepository = inventoryRepository;
    }

    public Inventory addQtyOnHand(RequestAddQtyOnHandDto dto) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(dto.getProductId(), dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("المخزون غير موجود"));

        Long newQuantity = inventory.getQuantityOnHand() + dto.getQuantityOnHand();
        inventory.setQuantityOnHand(newQuantity);

        return inventoryRepository.save(inventory);
    }

}
