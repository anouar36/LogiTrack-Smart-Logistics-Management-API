package com.logitrack.logitrack.dto.Warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseResponseDto {

    private Long id;
    private String code;
    private String name;
    private String location;
    private String description;
    private Long totalProducts;
    private Long totalQuantity;
}
