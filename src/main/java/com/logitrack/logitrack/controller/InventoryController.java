package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.Inventory.RequestInventoryDto;
import com.logitrack.logitrack.dto.Inventory.ResponseInventoryDto;
import com.logitrack.logitrack.entity.Inventory;
import com.logitrack.logitrack.service.InventoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/inventory")
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PostMapping("/inventory")
    public ResponseInventoryDto creatInventory(@RequestBody @Valid RequestInventoryDto dto){
        return inventoryService.creatInventory(dto);
    }
}
