package com.logitrack.logitrack.dto.PurchaseOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ReceivePurchaseOrderRequestDto {

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;
}