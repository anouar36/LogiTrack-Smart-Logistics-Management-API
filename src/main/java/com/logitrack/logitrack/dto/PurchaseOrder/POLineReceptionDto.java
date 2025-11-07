package com.logitrack.logitrack.dto.PurchaseOrder;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class POLineReceptionDto {

    @NotNull(message = "Purchase Order Line ID is required")
    private Long poLineId; // السطر (Line) ديال PO لي غنستلمو

    @NotNull(message = "Quantity received cannot be null")
    @Min(value = 1, message = "Quantity received must be at least 1")
    private Long quantityReceived; // شحال وصل ديال السلعة
}