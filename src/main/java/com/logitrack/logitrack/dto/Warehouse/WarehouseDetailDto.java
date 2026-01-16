package com.logitrack.logitrack.dto.Warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseDetailDto {

    private Long id;
    private String code;
    private String name;
    private String location;
    private String description;
    private Long totalProducts;
    private Long totalQuantity;
    private List<WarehouseInventoryDto> inventories;
}
