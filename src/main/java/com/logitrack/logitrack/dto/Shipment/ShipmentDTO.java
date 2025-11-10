package com.logitrack.logitrack.dto.Shipment;

import com.logitrack.logitrack.entity.enums.ShipmentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
public class ShipmentDTO {

    private Long id;
    private ShipmentStatus status;
    private String trackingNumber;
    private Instant shippedAt;
    private Instant deliveredAt;


    private Long salesOrderId;
    private Long carrierId;
}