package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAll();
    Boolean existsByNameAndSku(String name, String sku);
    Optional<Product> findById(Long id);
    List<Product> findByName(String name);
    List<Product> findByCategory(String category);
    List<Product> findByPriceGreaterThan(Double price);
    List<Product> findByPriceLessThan(Double price);
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    List<Product> findByNameContaining(String keyword);
    List<Product> findByCategoryAndPriceLessThan(String category, Double price);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
    <S extends Product> S save(S entity);
    <S extends Product> List<S> saveAll(Iterable<S> entities);
    void delete(Product entity);
    void deleteAll();

    List<Product> findByDeletedFalse(  );


}
