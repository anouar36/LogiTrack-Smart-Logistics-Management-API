package com.logitrack.logitrack.mapper;

import com.logitrack.logitrack.dto.SalesOrder.DesplayAllOrdersLineDto;
import com.logitrack.logitrack.entity.SalesOrderLine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class DesplayAllOrdersLineDtosMapper {

    public List<DesplayAllOrdersLineDto> toDto(List<SalesOrderLine> salesOrderLines){
        List<DesplayAllOrdersLineDto> desplayAllOrdersLineDtos = new ArrayList<>();


        for (SalesOrderLine sol : salesOrderLines){
            System.out.println("PRODUCT = " + sol.getProduct());
            DesplayAllOrdersLineDto dto = DesplayAllOrdersLineDto.builder()
                    .productSku(sol.getProduct().getSku())
                    .productName(sol.getProduct().getName())
                    .productCategory(sol.getProduct().getCategory())
                    .unitPrice(sol.getProduct().getPrice())
                    .remainingQuantityToReserve(sol.getRemainingQuantityToReserve())
                    .quantity(sol.getQuantity())
                    .totalPrice(sol.getTotalPrice())
                    .id(sol.getId())
                    .build();

            desplayAllOrdersLineDtos.add(dto);
        }
        return desplayAllOrdersLineDtos;
    }
}


