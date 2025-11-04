package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.dto.Product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderLineDto {
    private Long id;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private     ProductDto product;
}
