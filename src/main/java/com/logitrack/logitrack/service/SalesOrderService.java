package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.AllocationDto;
import com.logitrack.logitrack.dto.Product.AddProductToOrderRequest;
import com.logitrack.logitrack.dto.SalesOrder.*;
import com.logitrack.logitrack.dto.SalesOrder.DesplayAllOrdersLineDto;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.SOStatus;
import com.logitrack.logitrack.exception.OrderValidationException;
import com.logitrack.logitrack.exception.ProductNotExistsException;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.mapper.DesplayAllOrdersLineDtosMapper;
import com.logitrack.logitrack.repository.SalesOrderLineRepository;
import com.logitrack.logitrack.repository.SalesOrderRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.juli.logging.LogFactory;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays; // Add this import

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
    private final SalesOrderLineRepository salesOrderLineRepository;
    private  static final Logger logger = LoggerFactory.getLogger(SalesOrderService.class);


    @Transactional
    public ResponceSalesOrderDto salesOrder(RequestSalesOrderDto dto) {
        MDC.put("log_type", "BUSINESS");
        MDC.put("action", "CREATE_ORDER");

        logger.info("Order processing has begun");

        try {
            SalesOrder salesOrder = new SalesOrder();
            salesOrder.setClient(dto.getClient());
            salesOrder.setStatus(SOStatus.CREATED);
            salesOrder.setCreatedAt(LocalDateTime.now());

            salesOrder = salesOrderRepository.save(salesOrder);

            MDC.put("business_id", salesOrder.getId().toString());
            logger.info("Order header created successfully with ID: {}", salesOrder.getId());


            BigDecimal totalPriceOrder = BigDecimal.ZERO;
            List<SalesOrderLine> linesToSave = new ArrayList<>();
            List<String> backOrderProducts = new ArrayList<>();

            for (SalesOrderLine lineFromRequest : dto.getLines()) {
                MDC.put("product_id", lineFromRequest.getProduct().getId().toString());

                Long productId = lineFromRequest.getProduct().getId();
                Long quantityRequested = lineFromRequest.getQuantity();

                // Verification du stock
                List<AllocationDto> allocationDto = inventoryService.reserveProduct(productId, quantityRequested);
                Long remainingQuantityToReserve = quantityRequested;

                if (allocationDto != null) {
                    long totalQuantityTaken = allocationDto.stream()
                            .mapToLong(AllocationDto::getAllocatedQuantity)
                            .sum();
                    remainingQuantityToReserve = quantityRequested - totalQuantityTaken;
                }

                if (remainingQuantityToReserve.equals(quantityRequested)) {
                    logger.error("Stock Unavailable: Product {} has 0 stock available.", productId);
                    throw new RuntimeException("Product " + productId + " has no available stock.");
                }

                Product product = productService.getProductById(productId)
                        .orElseThrow(() -> new ProductNotExistsException("Product " + productId + " does not exist"));

                BigDecimal totalPrice = BigDecimal.valueOf(quantityRequested).multiply(lineFromRequest.getUnitPrice());

                SalesOrderLine line = SalesOrderLine.builder()
                        .quantity(quantityRequested)
                        .unitPrice(lineFromRequest.getUnitPrice())
                        .totalPrice(totalPrice)
                        .product(product)
                        .salesOrder(salesOrder)
                        .remainingQuantityToReserve(remainingQuantityToReserve)
                        .build();

                linesToSave.add(line);
                totalPriceOrder = totalPriceOrder.add(totalPrice);

                if (line.getRemainingQuantityToReserve() > 0) {
                    logger.warn("Partial reservation for product {}. Missing: {}", product.getName(), line.getRemainingQuantityToReserve());
                    backOrderProducts.add(product.getName() + " (Missing: " + line.getRemainingQuantityToReserve() + ")");
                }

                MDC.remove("product_id");
            }

            salesOrder.setLines(linesToSave);

            List<ResponseSalesOrderLineDto> resultSalesOrderLines = new ArrayList<>();
            for (SalesOrderLine line : linesToSave) {
                ResponseSalesOrderLineDto savedLine = salesOrderLineService.addOrderLine(line);
                resultSalesOrderLines.add(savedLine);
            }

            Client client = clientService.getClientById(salesOrder.getClient().getId());

            String backOrderMessage = backOrderProducts.isEmpty() ? null :
                    "Some products could not be fully reserved: " + String.join(", ", backOrderProducts);

            logger.info("Order processed successfully. Total Price: {}", totalPriceOrder);

            return ResponceSalesOrderDto.builder()
                    .message(backOrderMessage)
                    .clientId(client.getId())
                    .clientName(client.getName())
                    .ClientEmail(client.getUser().getEmail())
                    .lines(resultSalesOrderLines)
                    .createdAt(salesOrder.getCreatedAt())
                    .status(salesOrder.getStatus())
                    .totalPrice(totalPriceOrder)
                    .build();

        } catch (Exception e) {
            MDC.put("error_code", "ORDER_PROCESSING_FAILED");
            logger.error("Failed to process order", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    // SalesOrderService.java
    @Transactional
    public ResponceSalesOrderDto addProductsToOrder(Long orderId, List<AddProductToOrderRequest> productsToAdd) {

        // find  Order by id
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));

        // this for back Order aan message errors
        List<String> successMessages = new ArrayList<>();
        List<String> backorderMessages = new ArrayList<>();

        //loop for product request his want client added to his order
        for (AddProductToOrderRequest productDto : productsToAdd) {

            //check if product in database or not
            Product product = productService.getProductById(productDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productDto.getProductId()));

            // check if this product is active
            if (!product.isActive()) {
                backorderMessages.add("Product '" + product.getName() + "' is inactive and was skipped.");
                continue;
            }

           // call function reserve Product for reserved this product
            Long quantityNeeded = productDto.getQuantity();
            List<AllocationDto> allocations = inventoryService.reserveProduct(product.getId(), quantityNeeded);

            // calcule Quantity reserved
            long totalReservedNow = allocations.stream()
                    .mapToLong(AllocationDto::getAllocatedQuantity)
                    .sum();

            // calcule Remaining_Quantity_To_Reserve
            long remainingToReserve = quantityNeeded - totalReservedNow;

            // create message  for this option back order

            if (remainingToReserve > 0) {
                backorderMessages.add("Backorder: " + remainingToReserve + " units of " + product.getName());
            } else {
                successMessages.add("Product '" + product.getName() + "' added and reserved.");
            }


            // calcule price totale = unitPrice * quantity
            BigDecimal totalPrice = BigDecimal.valueOf(quantityNeeded)
                    .multiply(product.getPrice());


            // create SalesOrderlin in database
            SalesOrderLine line = SalesOrderLine.builder()
                    .product(product)
                    .salesOrder(order)
                    .quantity(quantityNeeded)
                    .unitPrice(product.getPrice())
                    .totalPrice(totalPrice)
                    .remainingQuantityToReserve(remainingToReserve)
                    .build();

            salesOrderLineRepository.save(line);
            order.getLines().add(line);
        }

        //updat SalesOrder for add new lin in  this order
        salesOrderRepository.save(order);

        // return json respence
        BigDecimal finalTotalPrice = order.getLines().stream()
                .map(SalesOrderLine::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String finalMessage = String.join(", ", successMessages) + " " + String.join(", ", backorderMessages);

        ResponceSalesOrderDto response = ResponceSalesOrderDto.builder()
                .clientId(order.getClient().getId())
                .clientName(order.getClient().getName())
                .ClientEmail(order.getClient().getUser().getEmail())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .lines(order.getLines().stream()
                        .map(line -> modelMapper.map(line, ResponseSalesOrderLineDto.class))
                        .toList())
                .totalPrice(finalTotalPrice)
                .message(finalMessage.trim())
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
    // SalesOrderService.java

    @Transactional
    public ValidatedOrderDto validateOrder(Long orderId) {

        SalesOrder order = salesOrderRepository.findByIdWithLinesAndProducts(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() != SOStatus.CREATED) {
            throw new OrderValidationException("Order is not in CREATED status. Current status: " + order.getStatus());
        }

        boolean hasNullLines = order.getLines().stream()
                .anyMatch(line -> line.getRemainingQuantityToReserve() == null);

        if (hasNullLines) {
            throw new OrderValidationException("Cannot validate order " + orderId + ". One or more lines have unprocessed quantity (null).");
        }


        List<String> newBackorderMessages = new ArrayList<>();

        for (SalesOrderLine line : order.getLines()) {

            if (line.getRemainingQuantityToReserve() > 0) {

                Long quantityToReserve = line.getRemainingQuantityToReserve();

                List<AllocationDto> allocation = inventoryService.reserveProduct(line.getProduct().getId(), quantityToReserve);
                long totalReserved = allocation.stream().mapToLong(AllocationDto::getAllocatedQuantity).sum();

                long newRemaining = quantityToReserve - totalReserved;
                line.setRemainingQuantityToReserve(newRemaining);
                salesOrderLineRepository.save(line);
                if (newRemaining > 0) {
                    newBackorderMessages.add("Product '" + line.getProduct().getName() + "' still has " + newRemaining + " units on backorder.");
                }
            }
        }


        if (!newBackorderMessages.isEmpty()) {
            String errorMessages = String.join(", ", newBackorderMessages);
            throw new OrderValidationException("Cannot validate order. Stock is still insufficient: " + errorMessages);
        }

        order.setStatus(SOStatus.RESERVED);
        salesOrderRepository.save(order);

        return ValidatedOrderDto.builder()
                .orderId(order.getId())
                .newStatus(order.getStatus())
                .message("Order validated successfully. All items reserved.")
                .build();
    }

    public Boolean checkStustOrderByProduct(Product product){
         List<SalesOrder> salesOrders =  salesOrderRepository.findAll();
         for ( SalesOrder salesOrder : salesOrders){
             if(salesOrder.getStatus() == SOStatus.CREATED || salesOrder.getStatus() == SOStatus.RESERVED){
                 for(SalesOrderLine salesOrderLine : salesOrder.getLines()){
                     if(salesOrderLine.getProduct().getId()== product.getId()){
                         return  true;
                     }
                 }
             }
         }
         return false;
    }
}
