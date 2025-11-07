package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.PurchaseOrder.CreatePurchaseOrderRequestDto;
import com.logitrack.logitrack.dto.PurchaseOrder.PurchaseOrderResponseDto;
import com.logitrack.logitrack.dto.PurchaseOrder.ReceiveFullPurchaseOrderDto;
import com.logitrack.logitrack.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<PurchaseOrderResponseDto> createPurchaseOrder(
            @Valid @RequestBody CreatePurchaseOrderRequestDto request) {

        PurchaseOrderResponseDto response = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{poId}/approve")
    public ResponseEntity<PurchaseOrderResponseDto> approvePurchaseOrder(@PathVariable Long poId) {

        PurchaseOrderResponseDto response = purchaseOrderService.approvePurchaseOrder(poId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{poId}/receive-full") // 👈  بدلت السمية باش تكون واضحة
    public ResponseEntity<String> receiveFullPurchaseOrder(
            @PathVariable Long poId,
            @Valid @RequestBody ReceiveFullPurchaseOrderDto request) { // 👈  استعملنا الـ DTO المبسط

        purchaseOrderService.receiveFullPurchaseOrder(poId, request.getWarehouseId());
        return ResponseEntity.ok("Stock received successfully (Full) and backorders processed.");
    }
}