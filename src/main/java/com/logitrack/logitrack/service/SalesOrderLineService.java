package com.logitrack.logitrack.service;


import com.logitrack.logitrack.dto.Product.ProductDto;
import com.logitrack.logitrack.dto.SalesOrder.ResponseSalesOrderLineDto;
import com.logitrack.logitrack.dto.SalesOrder.SalesOrderLineResponseDto;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.entity.SalesOrderLine;
import com.logitrack.logitrack.repository.SalesOrderLineRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SalesOrderLineService {

    private final SalesOrderLineRepository salesOrderLineRepository;
    private final ModelMapper modelMapper;

    public ResponseSalesOrderLineDto addOrderLine(SalesOrderLine line) {

        SalesOrderLine saved = salesOrderLineRepository.save(line);

        Product product = saved.getProduct();

        ProductDto productDto = ProductDto.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .active(product.isActive())
                .deleted(product.isDeleted())
                .build();

        return ResponseSalesOrderLineDto.builder()
                .id(saved.getId())
                .quantity(saved.getQuantity())
                .totalPrice(saved.getTotalPrice())
                .unitPrice(saved.getUnitPrice())
                .product(productDto)
                .build();
    }





}
