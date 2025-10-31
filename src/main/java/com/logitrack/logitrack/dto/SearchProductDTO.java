package com.logitrack.logitrack.dto;

import lombok.Data;

@Data
public class SearchProductDTO {

    private String name;
    private String category;
    private Double minPrice;
    private Double maxPrice;
}


