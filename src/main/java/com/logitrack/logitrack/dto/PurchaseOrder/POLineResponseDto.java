package com.logitrack.logitrack.dto.PurchaseOrder;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class POLineResponseDto {
    private Long id;
    private Long productId;
    private String productSku;
    private String productName;
    private Long quantity;
    private BigDecimal unitPrice;
}