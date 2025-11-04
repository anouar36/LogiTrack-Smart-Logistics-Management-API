package com.logitrack.logitrack.dto.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProductToOrderRequest {
    // orderId تحيد من هنا
    private Long productId;
    private Long quantity;
}