package com.logitrack.logitrack.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.logitrack.logitrack.entity.enums.SOStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private SOStatus status;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonBackReference
    private Client client;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<SalesOrderLine> lines;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    private List<Shipment> shipments;
}
