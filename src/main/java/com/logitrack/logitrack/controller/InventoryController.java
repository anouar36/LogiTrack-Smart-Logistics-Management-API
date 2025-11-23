package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.Inventory.RequestInventoryDto;
import com.logitrack.logitrack.dto.Inventory.ResponseInventoryDto;
import com.logitrack.logitrack.service.InventoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class InventoryController {
    private final InventoryService inventoryService;


    @PostMapping("/inventory")
    public ResponseInventoryDto creatInventory(@RequestBody @Valid RequestInventoryDto dto){
        return inventoryService.creatInventory(dto);

    }


}
