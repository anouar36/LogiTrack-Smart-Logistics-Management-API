package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.InventoryMovement.InventoryMovementRequestDTO;
import com.logitrack.logitrack.dto.InventoryMovement.InventoryMovementRespenceDTO;
import com.logitrack.logitrack.service.InventoryMovementService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/InventoryMovement")
public class InventoryMovementController {

    private final InventoryMovementService inventoryMovementService;


    @PostMapping("/creat")
    public InventoryMovementRespenceDTO addInventoryMovement(@RequestBody @Valid InventoryMovementRequestDTO dto){
        return  inventoryMovementService.addInventoryMovement(dto);
    }

}
