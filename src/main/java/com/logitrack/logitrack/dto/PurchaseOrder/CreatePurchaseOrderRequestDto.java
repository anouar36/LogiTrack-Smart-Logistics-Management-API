package com.logitrack.logitrack.dto.PurchaseOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreatePurchaseOrderRequestDto {

    @NotNull(message = "Supplier ID cannot be null")
    private Long supplierId;

    @NotEmpty(message = "A purchase order must have at least one line")
    @Valid // <-- ضرورية باش يفيريفي حتى السطور لداخل
    private List<PurchaseOrderLineRequestDto> lines;
}