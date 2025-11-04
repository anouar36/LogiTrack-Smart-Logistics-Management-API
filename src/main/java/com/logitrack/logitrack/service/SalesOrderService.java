package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.AllocationDto;
import com.logitrack.logitrack.dto.SalesOrder.*;
import com.logitrack.logitrack.dto.SalesOrder.DesplayAllOrdersLineDto;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.SOStatus;
import com.logitrack.logitrack.exception.ProductNotExistsException;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.mapper.DesplayAllOrdersLineDtosMapper;
import com.logitrack.logitrack.repository.SalesOrderRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.micrometer.observation.transport.Kind.CLIENT;

@Service
@AllArgsConstructor
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderLineService salesOrderLineService;
    private final ProductService productService;
    private final ModelMapper modelMapper;
    private final ClientService clientService;
    private final InventoryService inventoryService;
    private final DesplayAllOrdersLineDtosMapper desplayAllOrdersLineDtosMapper;

    @Transactional
    public ResponceSalesOrderDto salesOrder(RequestSalesOrderDto dto) {

        //this for mapper my request Sales Order Dto to entity
        //SalesOrder salesOrder = modelMapper.map(dto, SalesOrder.class);

        //firste add status of this order Created
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setClient(dto.getClient());
        salesOrder.setStatus(SOStatus.CREATED);


        //initializr varible total Price of Sales order 0.00
        BigDecimal totalPriceOrder = BigDecimal.ZERO;

        //fix my entity Sales Order Line for checked product and totle price of product
        List<SalesOrderLine> linesToSave = new ArrayList<>();
        for (SalesOrderLine lineFromRequest : dto.getLines()) {

            // this for check if this product we have a quantity in repository
            Long productId = lineFromRequest.getProduct().getId();
            Long remainingQuantityToReserve = 0L ;
            //check desponbelety of this producte
            List<AllocationDto> allocationDto = inventoryService.reserveProduct(productId,lineFromRequest.getQuantity());
            if(allocationDto != null){
                long totalQuantityTaken = allocationDto.stream()
                        .mapToLong(AllocationDto::getAllocatedQuantity)
                        .sum();

                remainingQuantityToReserve = lineFromRequest.getQuantity() - totalQuantityTaken ;
            }


            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new ProductNotExistsException("Product with id " + productId + " does not exist"));

            //calcule totale price of product Quantity * Unit price
            BigDecimal quantity = BigDecimal.valueOf(lineFromRequest.getQuantity());
            BigDecimal totalPrice = quantity.multiply(lineFromRequest.getUnitPrice());

            SalesOrderLine line = SalesOrderLine.builder()
                    .quantity(lineFromRequest.getQuantity())
                    .unitPrice(lineFromRequest.getUnitPrice())
                    .totalPrice(totalPrice)
                    .product(product)
                    .salesOrder(salesOrder)
                    .remainingQuantityToReserve(remainingQuantityToReserve)
                    .build();

            linesToSave.add(line);
            totalPriceOrder = totalPriceOrder.add(totalPrice);
        }

        //this for save sales order
        salesOrder = salesOrderRepository.save(salesOrder);
        salesOrder.setLines(linesToSave);

        //this for save sales Order line one by one
        List<ResponseSalesOrderLineDto> resultSalesOrderLines = new ArrayList<>();
        for (SalesOrderLine line : linesToSave) {
            ResponseSalesOrderLineDto savedLine = salesOrderLineService.addOrderLine(line);
            resultSalesOrderLines.add(savedLine);
        }

        Client client = clientService.getClientById(salesOrder.getClient().getId());

        //this for return dto data
        ResponceSalesOrderDto response = ResponceSalesOrderDto.builder()
                .clientId(client.getId())
                .clientName(client.getName())
                .ClientEmail(client.getUser().getEmail())
                .lines(resultSalesOrderLines)
                .createdAt(salesOrder.getCreatedAt())
                .status(salesOrder.getStatus())
                .totalPrice(totalPriceOrder)
                .build();

        return response;
    }
    public SalesOrderDto getSalesOrderByIdForClient(Long clientId, Long orderId) {
        // Find the order and ensure it belongs to the client
        SalesOrder order = salesOrderRepository.findById(orderId)
                .filter(o -> o.getClient().getId().equals(clientId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SalesOrder not found or does not belong to this client"));

        // Convert entity to DTO
        return modelMapper.map(order , SalesOrderDto.class);
    }

    public List<DesplayAllOrdersDto> getAllOrders(){
       List<SalesOrder> salesOrders = salesOrderRepository.findAll();
       List<DesplayAllOrdersDto> desplayAllOrdersDto = new ArrayList<>();

       for(SalesOrder salesOrder: salesOrders){
           List<DesplayAllOrdersLineDto> desplayAllOrdersLinesDto = desplayAllOrdersLineDtosMapper.toDto(salesOrder.getLines());

           DesplayAllOrdersDto dto = DesplayAllOrdersDto.builder()
                   .OrderId(salesOrder.getId())
                   .OrderCreatedAt(salesOrder.getCreatedAt())
                   .OrderStatus(salesOrder.getStatus())
                   .lines(desplayAllOrdersLinesDto)
                   .build();
           desplayAllOrdersDto.add(dto);
       }
       return desplayAllOrdersDto;
    }
    public List<DesplayAllOrdersDto> getOrdersByIdClient(Client client){

        List<SalesOrder> salesOrders = salesOrderRepository.findAllByClient(client);
        List<DesplayAllOrdersDto> desplayAllOrdersDto = new ArrayList<>();

        for(SalesOrder salesOrder: salesOrders){
            List<DesplayAllOrdersLineDto> desplayAllOrdersLinesDto = desplayAllOrdersLineDtosMapper.toDto(salesOrder.getLines());

            DesplayAllOrdersDto dto = DesplayAllOrdersDto.builder()
                    .OrderId(salesOrder.getId())
                    .OrderCreatedAt(salesOrder.getCreatedAt())
                    .OrderStatus(salesOrder.getStatus())
                    .lines(desplayAllOrdersLinesDto)
                    .build();
            desplayAllOrdersDto.add(dto);
        }
        return desplayAllOrdersDto;

    }
}
