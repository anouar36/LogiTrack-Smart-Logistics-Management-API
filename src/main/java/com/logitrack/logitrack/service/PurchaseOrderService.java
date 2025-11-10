package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.PurchaseOrder.*; // Import DTOs
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.POStatus;
import com.logitrack.logitrack.exception.BusinessException;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.repository.*;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant; // 👈  تعديل: كنستعملو Instant
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final WarehouseRepository warehouseRepository;
    private final InventoryService inventoryService;
    private final PurchaseOrderLineRepository poLineRepository;

    @Transactional
    public PurchaseOrderResponseDto createPurchaseOrder(CreatePurchaseOrderRequestDto request) {

        // 1 get suplyer by id for check if this supley is founde or not
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        // creat PurchaseOrder
        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .status(POStatus.DRAFT)
                .createdAt(Instant.now())
                .lines(new ArrayList<>())
                .build();

        // loopp for all PurchaseOrderlinr for created lin by lin with this PurchaseOrder
        for (PurchaseOrderLineRequestDto lineDto : request.getLines()) {

            // get product for by id
            Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + lineDto.getProductId()));

            // check statude of this priduct if his active or not
            if (!product.isActive()) {
                throw new BusinessException("Product '" + product.getName() + "' is inactive and cannot be purchased.");
            }

            // create new line for this PurchaseOrder
            PurchaseOrderLine poLine = PurchaseOrderLine.builder()
                    .product(product)
                    .quantity(lineDto.getQuantity())
                    .unitPrice(lineDto.getUnitPrice())
                    .purchaseOrder(po)
                    .build();

            // save this PurchaseOrderline to arryList
            po.getLines().add(poLine);
        }


        // save array lis of PurchaseOrderline
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);
        return mapToDto(savedPo);
    }

    // mapper fot respense Dto
    private PurchaseOrderResponseDto mapToDto(PurchaseOrder po) {
        PurchaseOrderResponseDto dto = modelMapper.map(po, PurchaseOrderResponseDto.class);
        dto.setSupplierId(po.getSupplier().getId());
        dto.setSupplierName(po.getSupplier().getName());

        dto.setLines(po.getLines().stream().map(line -> {
            POLineResponseDto lineDto = modelMapper.map(line, POLineResponseDto.class);
            lineDto.setProductId(line.getProduct().getId());
            lineDto.setProductSku(line.getProduct().getSku());
            lineDto.setProductName(line.getProduct().getName());
            return lineDto;
        }).collect(Collectors.toList()));

        return dto;
    }
    @Transactional
    public PurchaseOrderResponseDto approvePurchaseOrder(Long poId) {

        // GET PurchaseOrder by id
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + poId));

        // check if this PurchaseOrder his have status approved at admin or not
        if (po.getStatus() != POStatus.DRAFT) {
            throw new BusinessException("Only POs in DRAFT status can be approved. Current status: " + po.getStatus());
        }

        // change status of this purchaseOrder after check his status draft
        po.setStatus(POStatus.APPROVED);
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        return mapToDto(savedPo);
    }

    @Transactional
    public void receiveFullPurchaseOrder(Long poId, Long warehouseId) {


        // get PurchaseOrder By PurchaseOrderId
        PurchaseOrder po = purchaseOrderRepository.findByIdWithLinesAndProducts(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + poId));

        // find warehouse By Id
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        // check status becouse don't receive just if APPROVED at admin
        if (po.getStatus() != POStatus.APPROVED) {
            throw new BusinessException("Cannot receive stock for a PO that is not APPROVED. Current status: " + po.getStatus());
        }

        // loop for all PurchaseOrderLine
        for (PurchaseOrderLine poLine : po.getLines()) {

            //catch product
            Product product = poLine.getProduct();

           //catch Quantity of this product in PurchaseOrder
            Long quantityReceived = poLine.getQuantity();

            //if Quantity of this product == 0 or null goo to nexte product
            if (quantityReceived == null || quantityReceived <= 0) {
                continue;
            }


            // calla function for serch all SO his Backorders for gived his Quantity of his order
            inventoryService.receiveStockAndFulfillBackorders(product, warehouse, quantityReceived);
        }

        // after all this change status of this PurchaseOrder to RECEIVED
        po.setStatus(POStatus.RECEIVED);
        purchaseOrderRepository.save(po);
    }
}