package com.logitrack.logitrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AllocationDto {
   private Long warehouseId;
   private Long allocatedQuantity;

}
