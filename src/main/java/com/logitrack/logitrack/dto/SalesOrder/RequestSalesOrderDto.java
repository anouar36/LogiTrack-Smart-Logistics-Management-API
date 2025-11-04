package com.logitrack.logitrack.dto.SalesOrder;

import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.SalesOrderLine;
import com.logitrack.logitrack.entity.Shipment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestSalesOrderDto {

    private Client client;

    private List<SalesOrderLine> lines;

    private List<Shipment> shipments;
}
