package com.logitrack.logitrack.dto.Product;

import lombok.Data;
import org.springframework.stereotype.Component;


@Data
@Component
public class ResponseDTO {
    private Long id;
    private String sku;
    private String name;
    private String category;
    private Double price;
    private String message;
}
