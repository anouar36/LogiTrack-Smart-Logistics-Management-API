package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.dto.Product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResponseSalesOrderLineDto {
    private Long id;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private ProductDto product;
}
