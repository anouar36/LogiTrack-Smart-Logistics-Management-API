package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.entity.enums.SOStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesOrderDto {
    private Long id;
    private SOStatus status;
    private LocalDateTime createdAt;
    private List<SalesOrderLineDto> lines;
    //private List<ShipmentDto> shipments;
}