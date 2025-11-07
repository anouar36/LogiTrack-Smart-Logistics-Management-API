package com.logitrack.logitrack.dto.PurchaseOrder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseOrderLineRequestDto {

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Long quantity;

    @NotNull(message = "Unit price cannot be null")
    @Min(value = 0, message = "Unit price must be positive")
    private BigDecimal unitPrice;
}