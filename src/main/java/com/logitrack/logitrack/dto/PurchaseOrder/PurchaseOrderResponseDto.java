package com.logitrack.logitrack.dto.PurchaseOrder;

import com.logitrack.logitrack.entity.enums.POStatus;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class PurchaseOrderResponseDto {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private POStatus status;
    private Instant createdAt;
    private List<POLineResponseDto> lines;
}