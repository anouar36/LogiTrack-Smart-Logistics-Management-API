package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.entity.SalesOrderLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SalesOrderLineResponseDto {
    private List<ResponseSalesOrderLineDto> lines;
    private BigDecimal totalPrice;
}
