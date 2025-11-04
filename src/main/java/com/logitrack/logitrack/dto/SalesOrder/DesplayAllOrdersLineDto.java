package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DesplayAllOrdersLineDto {

    private Long id;
    private Long quantity;
    private String productSku;
    private String productName;
    private String productCategory;
    private BigDecimal totalPrice;
    private BigDecimal unitPrice;
    private Long remainingQuantityToReserve;
}
