package com.logitrack.logitrack.mapper;

import com.logitrack.logitrack.dto.SalesOrder.SalesOrderDto;
import com.logitrack.logitrack.entity.SalesOrder;

public class salesOrderMapper {

    public SalesOrderDto toDto(SalesOrder salesOrder) {
        // Utiliser le builder du DTO, pas de l'entité
        SalesOrderDto salesOrderDto = SalesOrderDto.builder()
                .id(salesOrder.getId())
                .status(salesOrder.getStatus())
                .createdAt(salesOrder.getCreatedAt())
                .build();

        return salesOrderDto; // N'oublie pas de retourner le DTO
    }

}
