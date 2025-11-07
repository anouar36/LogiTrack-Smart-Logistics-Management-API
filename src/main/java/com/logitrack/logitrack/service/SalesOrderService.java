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
import org.modelmapper.ModelMapper;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<String> backOrderProducts = new ArrayList<>();

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


                if (remainingQuantityToReserve == lineFromRequest.getQuantity()) {
                    throw new RuntimeException(
                            "Product " + lineFromRequest.getProduct().getName() + " has no available stock."
                    );
                }

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

            if (line.getRemainingQuantityToReserve() > 0 && line.getRemainingQuantityToReserve() < lineFromRequest.getQuantity()) {
                backOrderProducts.add(
                        line.getProduct().getName()
                                + " (Missing: " + line.getRemainingQuantityToReserve() + ")"
                );
            }
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

        //message
        String backOrderMessage = null;
        if (!backOrderProducts.isEmpty()) {
            backOrderMessage = "Some products could not be fully reserved: "
                    + String.join(", ", backOrderProducts);
        }

        //this for return dto data
        ResponceSalesOrderDto response = ResponceSalesOrderDto.builder()
                .message(backOrderMessage)
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

    // SalesOrderService.java

    @Transactional
    public ResponceSalesOrderDto addProductsToOrder(Long orderId, List<AddProductToOrderRequest> productsToAdd) {

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + orderId));

        List<String> successMessages = new ArrayList<>();
        List<String> backorderMessages = new ArrayList<>();

        // (تأكد أن عندك ModelMapper معرف)
        // @Autowired private ModelMapper modelMapper;

        for (AddProductToOrderRequest productDto : productsToAdd) {

            Product product = productService.getProductById(productDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productDto.getProductId()));

            if (!product.isActive()) {
                backorderMessages.add("Product '" + product.getName() + "' is inactive and was skipped.");
                continue;
            }

            // 4️⃣  👇👇  هنا كاين التعديل الكبير  👇👇
            // ما بقيناش كنحسبو الستوك هنا، ولينا كنعيطو نيشان للمحرك

            Long quantityNeeded = productDto.getQuantity();
            List<AllocationDto> allocations = inventoryService.reserveProduct(product.getId(), quantityNeeded);

            // 4a. حساب شحال تحجز بصح
            long totalReservedNow = allocations.stream()
                    .mapToLong(AllocationDto::getAllocatedQuantity)
                    .sum();

            // 4b. حساب شحال باقي ناقص (Backorder)
            long remainingToReserve = quantityNeeded - totalReservedNow;

            // 4c. إنشاء الرسالة
            if (remainingToReserve > 0) {
                backorderMessages.add("Backorder: " + remainingToReserve + " units of " + product.getName());
            } else {
                successMessages.add("Product '" + product.getName() + "' added and reserved.");
            }
            // 👆👆  نهاية التعديل  👆👆


            // 5️⃣ احسب الثمن الكلي
            BigDecimal totalPrice = BigDecimal.valueOf(quantityNeeded)
                    .multiply(product.getPrice()); // (من الأحسن الثمن يكون فـ DTO)

            // 6️⃣ إنشاء SalesOrderLine
            SalesOrderLine line = SalesOrderLine.builder()
                    .product(product)
                    .salesOrder(order)
                    .quantity(quantityNeeded)
                    .unitPrice(product.getPrice()) // (أو من DTO)
                    .totalPrice(totalPrice)
                    .remainingQuantityToReserve(remainingToReserve) // 👈  كنسجلو النقص لي تحسب
                    .build();

            // 7️⃣ زيد الـ line
            salesOrderLineRepository.save(line);
            order.getLines().add(line);
        }

        // 8️⃣ تحديث الـ order
        salesOrderRepository.save(order);

        // 9️⃣ رجع JSON response
        BigDecimal finalTotalPrice = order.getLines().stream()
                .map(SalesOrderLine::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String finalMessage = String.join(", ", successMessages) + " " + String.join(", ", backorderMessages);

        ResponceSalesOrderDto response = ResponceSalesOrderDto.builder()
                .clientId(order.getClient().getId())
                .clientName(order.getClient().getName())
                .ClientEmail(order.getClient().getUser().getEmail())
                .status(order.getStatus()) // (غتكون CREATED)
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

        // 1️⃣ جلب الطلبية (مع السطور والمنتجات)
        SalesOrder order = salesOrderRepository.findByIdWithLinesAndProducts(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // 2️⃣ التحقق من الحالة
        if (order.getStatus() != SOStatus.CREATED) {
            throw new OrderValidationException("Order is not in CREATED status. Current status: " + order.getStatus());
        }

        // 3️⃣ التحقق من الـ Null (كنخليوه للأمان)
        boolean hasNullLines = order.getLines().stream()
                .anyMatch(line -> line.getRemainingQuantityToReserve() == null);

        if (hasNullLines) {
            throw new OrderValidationException("Cannot validate order " + orderId + ". One or more lines have unprocessed quantity (null).");
        }

        // 4️⃣ 👇👇  اللوجيك الجديد: محاولة حجز النقص (Backorders) 👇👇

        // ليسته باش نجمعو الأخطاء الجداد
        List<String> newBackorderMessages = new ArrayList<>();

        // غندورو على كاع السطور لي فيهم نقص
        for (SalesOrderLine line : order.getLines()) {

            if (line.getRemainingQuantityToReserve() > 0) {
                // هاد السطر فيه نقص، غنحاولو نحجزوه دابا

                Long quantityToReserve = line.getRemainingQuantityToReserve();

                // 4a. العيطة لسيرفيس الستوك
                List<AllocationDto> allocation = inventoryService.reserveProduct(line.getProduct().getId(), quantityToReserve);
                long totalReserved = allocation.stream().mapToLong(AllocationDto::getAllocatedQuantity).sum();

                // 4b. تحديث السطر (Line)
                long newRemaining = quantityToReserve - totalReserved;
                line.setRemainingQuantityToReserve(newRemaining);
                salesOrderLineRepository.save(line); // كنسجلو التحديث ديال السطر

                // 4c. إلا باقي النقص، كنسجلو رسالة خطأ
                if (newRemaining > 0) {
                    newBackorderMessages.add("Product '" + line.getProduct().getName() + "' still has " + newRemaining + " units on backorder.");
                }
            }
        }
        // 👆👆 نهاية اللوجيك الجديد 👆👆


        // 5️⃣ القرار (Decision) - دابا غنشوفو واش باقي شي نقص
        if (!newBackorderMessages.isEmpty()) {
            // ❌ خطأ: فشلت محاولة الحجز، باقي النقص
            String errorMessages = String.join(", ", newBackorderMessages);
            throw new OrderValidationException("Cannot validate order. Stock is still insufficient: " + errorMessages);
        }

        // 6️⃣ ✅ نجاح: كلشي تحجز
        order.setStatus(SOStatus.RESERVED);
        salesOrderRepository.save(order);

        // 7️⃣ رجع جواب ناجح
        return ValidatedOrderDto.builder()
                .orderId(order.getId())
                .newStatus(order.getStatus())
                .message("Order validated successfully. All items reserved.")
                .build();
    }
}
