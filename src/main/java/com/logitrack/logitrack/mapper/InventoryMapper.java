package com.logitrack.logitrack.mapper;

import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.dto.InventoryMovement.InventoryMovementRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public RequestAddQtyOnHandDto toAddQtyOnHandDto(InventoryMovementRequestDTO dto) {
        RequestAddQtyOnHandDto addQtyDto = new RequestAddQtyOnHandDto();
        addQtyDto.setProductId(dto.getIdProduc());
        addQtyDto.setWarehouseId(dto.getIdWarehouse());
        addQtyDto.setQuantityOnHand(dto.getQuantity());
        return addQtyDto;
    }
}

