package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.Product.ProductAvailabilityDto;
import com.logitrack.logitrack.dto.Product.RequestDTO;
import com.logitrack.logitrack.dto.Product.ResponseDTO;
import com.logitrack.logitrack.entity.Product;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.mapper.CreatProductMapper;
import com.logitrack.logitrack.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final InventoryService inventoryService;
    private  ProductRepository productRepository;
    private  CreatProductMapper productMapper;
    private  SalesOrderLineService salesOrderLineService;
    private final SalesOrderService salesOrderService;
    public ProductService(ProductRepository productRepository, @Lazy SalesOrderService salesOrderService, SalesOrderLineService salesOrderLineService, CreatProductMapper creatProductMapper, InventoryService inventoryService) { // <-- أضف @Lazy هنا
        this.productRepository = productRepository;
        this.salesOrderService = salesOrderService;
        this.productMapper = productMapper;
        this.salesOrderLineService = salesOrderLineService;
        this.inventoryService = inventoryService;
    }

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

    public ProductAvailabilityDto checkProductAvailabilityBySku(String sku) {

        Optional<Product> productOpt = productRepository.findBySku(sku);

        if (productOpt.isEmpty()) {

            throw new RuntimeException("Produit non trouvé avec SKU: " + sku);
        }

        Product product = productOpt.get();

        if (product.isActive()) {
            return ProductAvailabilityDto.builder()
                    .sku(product.getSku())
                    .name(product.getName())
                    .category(product.getCategory())
                    .available(true)
                    .message("Produit disponible à la vente.")
                    .build();
        } else {
            return ProductAvailabilityDto.builder()
                    .sku(product.getSku())
                    .name(product.getName())
                    .category(product.getCategory())
                    .available(false)
                    .message("Ce produit est inactif et n'est pas disponible à la vente.")
                    .build();
        }
    }


    @Transactional
    public Boolean actionActiveProduct(Long id ) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));

        if (product.isActive() == true) {
            Boolean cheking = salesOrderService.checkStustOrderByProduct(product);
            Boolean chekinInvetory = inventoryService.chectQuentutProduct(product);
            if(cheking == true || chekinInvetory == true) {
                throw new RuntimeException("this product all ready in Order created or reserved or his hav Quentity");
            }
            product.setActive(false);
        } else {
            product.setActive(true);
        }
        productRepository.save(product);
        return  true;

    }


}
