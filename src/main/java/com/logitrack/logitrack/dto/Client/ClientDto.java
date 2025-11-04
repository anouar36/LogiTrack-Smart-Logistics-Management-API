package com.logitrack.logitrack.dto.Client;


import com.logitrack.logitrack.dto.SalesOrder.SalesOrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
class ClientDto {
    private Long id;
    private String name;
    private List<SalesOrderDto> salesOrders;
}

