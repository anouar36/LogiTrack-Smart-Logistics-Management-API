package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    private String name;

    private String category;

    private Double price;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean deleted = false;
}