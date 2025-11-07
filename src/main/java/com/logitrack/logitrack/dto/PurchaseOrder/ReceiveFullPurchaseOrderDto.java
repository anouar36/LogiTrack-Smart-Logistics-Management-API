package com.logitrack.logitrack.dto.PurchaseOrder;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReceiveFullPurchaseOrderDto {

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;
}