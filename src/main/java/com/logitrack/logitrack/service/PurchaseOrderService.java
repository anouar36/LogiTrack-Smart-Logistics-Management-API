package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.PurchaseOrder.*; // Import DTOs
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.POStatus;
import com.logitrack.logitrack.exception.BusinessException;
import com.logitrack.logitrack.exception.ResourceNotFoundException;
import com.logitrack.logitrack.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant; // 👈  تعديل: كنستعملو Instant
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ModelMapper modelMapper; // (أو MapStruct كيفما طلبتي)
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private PurchaseOrderLineRepository poLineRepository;

    @Transactional
    public PurchaseOrderResponseDto createPurchaseOrder(CreatePurchaseOrderRequestDto request) {

        // 1. جلب المورد (Supplier)
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));

        // 2. إنشاء الطلبية (Header)
        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .status(POStatus.DRAFT)
                .createdAt(Instant.now()) // 👈  تعديل: كنستعملو Instant
                .lines(new ArrayList<>())
                .build();

        // 3. لوب (Loop) على السطور
        for (PurchaseOrderLineRequestDto lineDto : request.getLines()) {

            Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + lineDto.getProductId()));

            if (!product.isActive()) {
                throw new BusinessException("Product '" + product.getName() + "' is inactive and cannot be purchased.");
            }

            // 3c. إنشاء السطر (Line)
            PurchaseOrderLine poLine = PurchaseOrderLine.builder()
                    .product(product)
                    .quantity(lineDto.getQuantity())
                    .unitPrice(lineDto.getUnitPrice()) // 👈  تعديل: زدنا ثمن الوحدة
                    .purchaseOrder(po)
                    .build();

            po.getLines().add(poLine);
        }

        // 4. حفظ الطلبية (مع السطور بفضل CascadeType.ALL لي عندك)
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        // 5. تحويل الجواب لـ DTO
        return mapToDto(savedPo);
    }

    // --- ميتود مساعدة للتحويل (Mapping) ---
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

        // 1. جلب طلب الشراء (PO)
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + poId));

        // 2. التحقق من الحالة (كنستعمل DRAFT بناءً على الصورة ديالك)
        if (po.getStatus() != POStatus.DRAFT) {
            throw new BusinessException("Only POs in DRAFT status can be approved. Current status: " + po.getStatus());
        }

        // 3. الموافقة (Approve)
        po.setStatus(POStatus.APPROVED);
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        // 4. رجع الجواب (DTO)
        return mapToDto(savedPo); // (استعمل الـ Mapper لي صاوبنا قبيلة)
    }

    // ... (الميتود "mapToDto" لي صاوبنا قبيلة) ...



    @Transactional
    public void receiveFullPurchaseOrder(Long poId, Long warehouseId) {

        // 1. جلب الطلبية (PO) بالسطور والمنتجات ديالها
        PurchaseOrder po = purchaseOrderRepository.findByIdWithLinesAndProducts(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with id: " + poId));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + warehouseId));

        if (po.getStatus() != POStatus.APPROVED) {
            throw new BusinessException("Cannot receive stock for a PO that is not APPROVED. Current status: " + po.getStatus());
        }

        // 2. لوب (Loop) على السطور "الحقيقية" ديال الـ PO
        for (PurchaseOrderLine poLine : po.getLines()) {

            Product product = poLine.getProduct();

            // 👇👇  هنا فين طبقنا الافتراض ديالك  👇👇
            // كنفترضو أن الكمية لي وصلات هي الكمية لي طلبنا
            Long quantityReceived = poLine.getQuantity();

            if (quantityReceived == null || quantityReceived <= 0) {
                continue; // كنتجاهلو السطور لي مافيهمش كمية
            }

            // 3. ✨✨  كنعيطو للـ InventoryService بنفس اللوجيك القديم ✨✨
            // هو غيزيد الستوك ويقلب على الطلبيات (SO) أوتوماتيكيا
            inventoryService.receiveStockAndFulfillBackorders(product, warehouse, quantityReceived);
        }

        // 4. تبديل الحالة لـ "تم الاستلام"
        po.setStatus(POStatus.RECEIVED); // دابا غندوزو نيشان لـ RECEIVED
        purchaseOrderRepository.save(po);
    }
}