package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.Product.ProductAvailabilityDto;
import com.logitrack.logitrack.dto.Product.RequestDTO;
import com.logitrack.logitrack.dto.Product.ResponseDTO;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Get all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Get all active products (not soft deleted)
    @GetMapping("/active")
    public ResponseEntity<List<Product>> getAllActiveProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    // Get product by id
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get products by name
    @GetMapping("/name/{name}")
    public ResponseEntity<List<Product>> getProductsByName(@PathVariable String name) {
        return ResponseEntity.ok(productService.getProductsByName(name));
    }

    // Get products by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    // Get products by price filters
    @GetMapping("/price/greater/{price}")
    public ResponseEntity<List<Product>> getProductsByPriceGreaterThan(@PathVariable Double price) {
        return ResponseEntity.ok(productService.getProductsByPriceGreaterThan(price));
    }

    @GetMapping("/price/less/{price}")
    public ResponseEntity<List<Product>> getProductsByPriceLessThan(@PathVariable Double price) {
        return ResponseEntity.ok(productService.getProductsByPriceLessThan(price));
    }

    @GetMapping("/price/between")
    public ResponseEntity<List<Product>> getProductsByPriceBetween(@RequestParam Double min, @RequestParam Double max) {
        return ResponseEntity.ok(productService.getProductsByPriceBetween(min, max));
    }

    // Get products by name containing keyword
    @GetMapping("/search")
    public ResponseEntity<List<Product>> getProductsByNameContaining(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.getProductsByNameContaining(keyword));
    }

    // Get products by category and price less than
    @GetMapping("/category/{category}/price/less/{price}")
    public ResponseEntity<List<Product>> getProductsByCategoryAndPriceLessThan(@PathVariable String category, @PathVariable Double price) {
        return ResponseEntity.ok(productService.getProductsByCategoryAndPriceLessThan(category, price));
    }

    // Add a new product via DTO
    @PostMapping
    public ResponseEntity<ResponseDTO> addProduct(@RequestBody RequestDTO requestDTO) {
        ResponseDTO response = productService.addProducte(requestDTO);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().build();
    }

    // Add a new product via entity
    @PostMapping("/entity")
    public ResponseEntity<Product> addProductEntity(@RequestBody Product product) {
        return ResponseEntity.ok(productService.addProduct(product));
    }

    // Add multiple products
    @PostMapping("/bulk")
    public ResponseEntity<List<Product>> addAllProducts(@RequestBody List<Product> products) {
        return ResponseEntity.ok(productService.addAllProducts(products));
    }

    // Update product by id
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody RequestDTO requestDTO) {

        ResponseDTO updated = productService.updateProduct(id, requestDTO);

        if (updated != null) {
            return ResponseEntity.ok(updated);
        }

        return ResponseEntity.notFound().build();
    }


    // Delete product by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Long id) {
        if (productService.productExistsById(id)) {
            productService.deleteProductById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Delete product by entity
    @DeleteMapping
    public ResponseEntity<Void> deleteProduct(@RequestBody Product product) {
        productService.deleteProduct(product);
        return ResponseEntity.noContent().build();
    }

    // Delete all products
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllProducts() {
        productService.deleteAllProducts();
        return ResponseEntity.noContent().build();
    }

    // Soft delete product
    @DeleteMapping("/soft/{id}")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable Long id) {
        if (productService.productExistsById(id)) {
            productService.softDeleteProduct(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Count products
    @GetMapping("/count")
    public ResponseEntity<Long> countProducts() {
        return ResponseEntity.ok(productService.countProducts());
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductAvailabilityDto> getProductAvailabilityBySku(@PathVariable String sku) {

        ProductAvailabilityDto dto = productService.checkProductAvailabilityBySku(sku);
        return ResponseEntity.ok(dto);

    }

    @PutMapping("active/{id}")
        public Boolean actionActiveProduct(@PathVariable Long id) {

            boolean result = productService.actionActiveProduct(id);
            return result;
        }

}
