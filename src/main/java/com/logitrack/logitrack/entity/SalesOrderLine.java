package com.logitrack.logitrack.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long quantity;
    private BigDecimal unitPrice;
    
    @ManyToOne
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
