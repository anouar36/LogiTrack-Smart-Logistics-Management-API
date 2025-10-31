package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Product.RequestDTO;
import com.logitrack.logitrack.dto.Product.ResponseDTO;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.mapper.CreatProductMapper;
import com.logitrack.logitrack.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CreatProductMapper productMapper;

    public ResponseDTO addProducte(RequestDTO creatProductDTO){
        Boolean productExists=  productRepository.existsByNameAndSku(creatProductDTO.getName(),creatProductDTO.getSku());
        if(productExists){
           Product product = productMapper.toEntity(creatProductDTO);
           Product product1 = productRepository.save(product);
           return productMapper.toDto(product1);
        }
        return null;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> addAllProducts(List<Product> products) {
        return productRepository.saveAll(products);
    }

    public ResponseDTO updateProduct(Long id, RequestDTO requestDTO) {
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setName(requestDTO.getName());
            existingProduct.setSku(requestDTO.getSku());
            existingProduct.setPrice(requestDTO.getPrice());
            existingProduct.setCategory(requestDTO.getCategory());
            Product updatedProduct = productRepository.save(existingProduct);
            return productMapper.toDto(updatedProduct);
        }).orElse(null);
    }


    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }

    public void deleteAllProducts() {
        productRepository.deleteAll();
    }

    public boolean productExistsById(Long id) {
        return productRepository.existsById(id);
    }

    public boolean productExistsByNameAndSku(String name, String sku) {
        return productRepository.existsByNameAndSku(name, sku);
    }

    public long countProducts() {
        return productRepository.count();
    }

    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getProductsByPriceGreaterThan(Double price) {
        return productRepository.findByPriceGreaterThan(price);
    }

    public List<Product> getProductsByPriceLessThan(Double price) {
        return productRepository.findByPriceLessThan(price);
    }

    public List<Product> getProductsByPriceBetween(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Product> getProductsByNameContaining(String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    public List<Product> getProductsByCategoryAndPriceLessThan(String category, Double price) {
        return productRepository.findByCategoryAndPriceLessThan(category, price);
    }

    public void softDeleteProduct(Long id) {
        productRepository.findById(id).ifPresent(product -> {
            product.setDeleted(true);
            productRepository.save(product);
        });
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByDeletedFalse();
    }


}
