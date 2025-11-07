package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.entity.enums.SOStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidatedOrderDto {
    private Long orderId;
    private SOStatus newStatus;
    private String message;
}