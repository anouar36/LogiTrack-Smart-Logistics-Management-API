package com.logitrack.logitrack.dto.Product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // غنستعملو Builder باش نعمروه بسهولة
public class ProductAvailabilityDto {
    private String sku;
    private String name;
    private String category;

    // هادي غتكون 'true' أو 'false' على حساب الحالة
    private boolean available;

    // هادي هي الرسالة الواضحة لي غترجع (Critère d'acceptation)
    private String message;
}