package com.logitrack.logitrack.service;

import com.logitrack.logitrack.dto.AllocationDto;
import com.logitrack.logitrack.dto.Inventory.RequestAddQtyOnHandDto;
import com.logitrack.logitrack.dto.Inventory.RequestInventoryDto;
import com.logitrack.logitrack.dto.Inventory.ResponseInventoryDto;
import com.logitrack.logitrack.entity.*;
import com.logitrack.logitrack.entity.enums.MovementType;
import com.logitrack.logitrack.entity.enums.SOStatus;
import com.logitrack.logitrack.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class InventoryService {
    private final ModelMapper modelMapper;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMovementRepository movementRepository;
    private final  SalesOrderLineRepository salesOrderLineRepository;
    private final  SalesOrderRepository salesOrderRepository;




    public Inventory addQtyOnHand(RequestAddQtyOnHandDto dto) {
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(dto.getProductId(), dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Inventory does not exist,Please, can you create new Inventory?"));

        Long newQuantity = inventory.getQuantityOnHand() + dto.getQuantityOnHand();
        inventory.setQuantityOnHand(newQuantity);

        return inventoryRepository.save(inventory);
    }
    public ResponseInventoryDto creatInventory(RequestInventoryDto dto){
        Optional<Inventory> existingInventory =
                inventoryRepository.existsByProductIdAndWarehouseId(dto.getProductId(), dto.getWarehouseId());

        if (existingInventory.isPresent()) {
            throw new RuntimeException("Inventory already exists for this product in this warehouse");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Inventory inventory = modelMapper.map(dto,Inventory.class);
        return modelMapper.map(inventoryRepository.save(inventory), ResponseInventoryDto.class);


    }
    @Transactional
    public List<AllocationDto> reserveProduct(Long productId, Long quantityNeeded) {

        List<AllocationDto> allocations = new ArrayList<>();
        long remainingToReserve = quantityNeeded;

        // 1. جيب غير الستوك لي بغينا (مصفى (filtered) ومرتب)
        List<Inventory> inventories = inventoryRepository.findAvailableStockForProduct(productId);

        if (inventories.isEmpty()) {
            System.out.println("No available stock found for product: " + productId);
            return allocations; // رجع 0
        }

        // 2. لوب (Loop) على المستودعات لي فيهم السلعة
        for (Inventory inv : inventories) {

            if (remainingToReserve <= 0) {
                break; // صافي حجزنا الكمية لي بغينا
            }

            long availableInThisWarehouse = inv.getQuantityOnHand() - inv.getQuantityReserved();
            long qtyToReserveFromThis = Math.min(availableInThisWarehouse, remainingToReserve);

            // 3. ✨✨ الحجز الفعلي ✨✨
            inv.setQuantityReserved(inv.getQuantityReserved() + qtyToReserveFromThis);
            inventoryRepository.save(inv); // 👈 ضروري تسجل التغيير

            // 4. تسجيل العملية
            allocations.add(new AllocationDto(inv.getWarehouse().getId(), qtyToReserveFromThis));

            // 5. نقص داكشي لي تحجز
            remainingToReserve -= qtyToReserveFromThis;
        }

        if (remainingToReserve > 0) {
            System.out.println("We were unable to allocate the entire quantity requested; the remaining amount is:" + remainingToReserve);
        }

        return allocations;
    }

    // ... (باقي الكود ديالك ديال "receiveStockAndFulfillBackorders"...)



    @Transactional
    public void receiveStockAndFulfillBackorders(Product product, Warehouse warehouse, Long quantityReceived) {

        // --- الجزء 1: استلام الستوك (US14 / US6) ---

        // 1. جلب أو إنشاء الـ Inventory
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductAndWarehouse(product, warehouse);

        Inventory inventory;
        if (inventoryOpt.isPresent()) {
            // 👈  الحالة 1: لقينا الـ Inventory
            inventory = inventoryOpt.get();
        } else {
            // 👈  الحالة 2: ما لقيناش، غنصاوبو واحد جديد
            inventory = Inventory.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .quantityOnHand(0L)
                    .quantityReserved(0L)
                    .movements(new ArrayList<>()) // (مهمة: خاصو يتصاوب خاوي)
                    .build();
            // (ما محتاجينش .lastUpdatedAt حيتاش عندك قيمة افتراضية)
        }

        // 2. زيادة الستوك
        inventory.setQuantityOnHand(inventory.getQuantityOnHand() + quantityReceived);
        inventoryRepository.save(inventory);

        // 3. تسجيل الحركة (Movement)
        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .inventory(inventory) // 👈  ها هو التصحيح
                .quantity(quantityReceived)
                .type(MovementType.INBOUND) // (تأكد أن الـ Enum ديالك سميتو MovementType)
                .build();
        movementRepository.save(movement);

        // --- الجزء 2: تنفيذ الطلبيات (US9 الأوتوماتيكي) ---

        // 4. جلب الستوك المتاح (Available) الإجمالي لهاد المنتج
        // (هاد الميتود خاصك تصاوبها: كدير (sum(onHand) - sum(reserved))
        long availableStock = getGlobalAvailableStock(product.getId());

        if (availableStock <= 0) {
            return; // الستوك لي دخل يلاه كافح للحجوزات القديمة، ما كاين ما يتفرق
        }

        // 5. جلب كاع الطلبيات (SO) لي كتسنى هاد المنتج
        List<SalesOrderLine> linesToFulfill = salesOrderLineRepository.findBackordersForProduct(product.getId());

        Set<Long> updatedOrderIds = new HashSet<>(); // باش نعرفو شكون الطلبيات لي تعدلو

        for (SalesOrderLine line : linesToFulfill) {
            if (availableStock <= 0) {
                break; // صافي كملنا الستوك لي يلاه دخل
            }

            long needed = line.getRemainingQuantityToReserve();
            long canReserveNow = Math.min(availableStock, needed);

            // 6. ✨ كنعيطو للميتود ديالنا القديمة باش تحجز!
            // (غنعدلوها شوية باش تخدم مزيان)
            reserveProduct(product.getId(), canReserveNow);

            // 7. تحديث السطر (Line)
            line.setRemainingQuantityToReserve(needed - canReserveNow);
            salesOrderLineRepository.save(line);

            availableStock -= canReserveNow; // نقصو من الستوك المتاح
            updatedOrderIds.add(line.getSalesOrder().getId());
        }

        // --- الجزء 3: تحديث حالة الطلبيات (SO) ---

        // 8. كنتحققو من الطلبيات لي تعدلات
        for (Long orderId : updatedOrderIds) {
            checkAndSetOrderStatus(orderId);
        }
    }

    // --- ميتودات مساعدة خاصك تزيدها في هاد السيرفيس ---

    /**
     * كيحسب الستوك المتاح الإجمالي لمنتج معين
     */
    public long getGlobalAvailableStock(Long productId) {
        List<Inventory> inventories = inventoryRepository.findByProductId(productId);
        long totalOnHand = inventories.stream().mapToLong(Inventory::getQuantityOnHand).sum();
        long totalReserved = inventories.stream().mapToLong(Inventory::getQuantityReserved).sum();
        return totalOnHand - totalReserved;
    }

    /**
     * كيتأكد واش الطلبية كاملة تحجزات، ويبدل ليها الحالة
     */
    private void checkAndSetOrderStatus(Long orderId) {
        SalesOrder order = salesOrderRepository.findByIdWithLinesAndProducts(orderId).orElse(null);
        if (order == null) return;

        // كنتحققو واش باقي شي سطر فيه نقص
        boolean allReserved = order.getLines().stream()
                .allMatch(line -> line.getRemainingQuantityToReserve() == 0);

        if (allReserved) {
            order.setStatus(SOStatus.RESERVED);
            salesOrderRepository.save(order);
        }
    }

    // (الميتود "reserveProduct" ديالك خاصها تعدل شوية باش ما ترجعش List<AllocationDto>
    // وترجع شحال قدرات تحجز)
}


