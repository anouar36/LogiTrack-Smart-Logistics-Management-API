package com.logitrack.logitrack.mapper;

import com.logitrack.logitrack.dto.Product.RequestDTO;
import com.logitrack.logitrack.dto.Product.ResponseDTO;
import com.logitrack.logitrack.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class CreatProductMapper {

    public Product toEntity(RequestDTO creatProductDTO){
        if(creatProductDTO == null) return  null;
        Product product = new Product();
        product.setName(creatProductDTO.getName());
        product.setSku(creatProductDTO.getSku());
        product.setPrice(creatProductDTO.getPrice());
        product.setCategory(creatProductDTO.getCategory());
        return product;
    }

    public ResponseDTO toDto(Product product){
        if(product == null) return null ;
        ResponseDTO resrponseProductDTO = new ResponseDTO();
        resrponseProductDTO.setId(product.getId());
        resrponseProductDTO.setName(product.getName());
        resrponseProductDTO.setPrice(product.getPrice());
        resrponseProductDTO.setSku(product.getSku());
        resrponseProductDTO.setCategory(product.getCategory());
        return resrponseProductDTO;
    }
}
