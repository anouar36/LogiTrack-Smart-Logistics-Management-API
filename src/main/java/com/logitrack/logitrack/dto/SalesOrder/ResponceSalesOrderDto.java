package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.SalesOrder;
import com.logitrack.logitrack.entity.SalesOrderLine;
import com.logitrack.logitrack.entity.Shipment;
import com.logitrack.logitrack.entity.enums.SOStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ResponceSalesOrderDto {
    private SOStatus status;
    private LocalDateTime createdAt = LocalDateTime.now();
    private Long clientId;
    private String clientName;
    private String ClientEmail;
    private List<ResponseSalesOrderLineDto> lines;
    private BigDecimal totalPrice;
    private List<Shipment> shipments;
}
