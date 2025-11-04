package com.logitrack.logitrack.controller;

import com.logitrack.logitrack.dto.Product.AddProductToOrderRequest;
import com.logitrack.logitrack.dto.SalesOrder.DesplayAllOrdersDto;
import com.logitrack.logitrack.dto.SalesOrder.RequestSalesOrderDto;
import com.logitrack.logitrack.dto.SalesOrder.ResponceSalesOrderDto;
import com.logitrack.logitrack.dto.SalesOrder.SalesOrderLineDto;
import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.User;
import com.logitrack.logitrack.service.SalesOrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class SalesOrderController {
    private final SalesOrderService salesOrderService;

    @PostMapping("/salesOrder")
    public ResponseEntity<ResponceSalesOrderDto> salesOrder(@RequestBody RequestSalesOrderDto dto) {
        ResponceSalesOrderDto responceSalesOrderDto = salesOrderService.salesOrder(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responceSalesOrderDto);
    }

    @GetMapping("/salesOrder")
    public ResponseEntity<List<DesplayAllOrdersDto>> getAllsalesOrder() {
        List<DesplayAllOrdersDto> desplayAllOrdersDtos = salesOrderService.getAllOrders();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(desplayAllOrdersDtos);
    }
    @GetMapping("/salesOrderByClient")
    public ResponseEntity<List<DesplayAllOrdersDto>> getAllsalesOrderByClient() {

        User user = new User(1L,"anouar@gmail.com","anwar36flow",true,null );
        Client client = new Client(1L,"anouar",user,null);

        List<DesplayAllOrdersDto> desplayAllOrdersDtos = salesOrderService.getOrdersByIdClient(client);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(desplayAllOrdersDtos);
    }

    // SalesOrderController.java

    // SalesOrderController.java

    // الرابط غنسميوه "add-products" (للجمع)
    @PostMapping("/{orderId}/add-products")
    public ResponseEntity<ResponceSalesOrderDto> addProductsToOrder(
            @PathVariable Long orderId, // <-- الـ ID ديال الطلبية من الرابط
            @RequestBody List<AddProductToOrderRequest> productsToAdd) { // <-- ديما ليستة

        ResponceSalesOrderDto response = salesOrderService.addProductsToOrder(orderId, productsToAdd);
        return ResponseEntity.ok(response);
    }


}

