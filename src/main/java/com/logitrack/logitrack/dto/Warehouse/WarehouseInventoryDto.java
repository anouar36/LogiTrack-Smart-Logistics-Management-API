package com.logitrack.logitrack.dto.Warehouse;

import com.logitrack.logitrack.dto.Product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseInventoryDto {

    private Long inventoryId;
    private ProductDto product;
    private Long quantityOnHand;
    private Long quantityReserved;
    private Long availableQuantity;
}
