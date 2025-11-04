package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.entity.SalesOrderLine;
import com.logitrack.logitrack.entity.enums.SOStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DesplayAllOrdersDto {

    private Long OrderId;
    private SOStatus OrderStatus;
    private LocalDateTime OrderCreatedAt ;
    private List<DesplayAllOrdersLineDto> lines;
}
