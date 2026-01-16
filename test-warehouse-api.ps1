# Warehouse API Test Script
# Run this script to test all warehouse endpoints

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Warehouse API Testing Script" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080/api/warehouses"
$headers = @{
    "Content-Type" = "application/json"
}

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/health" -Method GET
    Write-Host "✓ Health Check: $health" -ForegroundColor Green
} catch {
    Write-Host "✗ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: Create Warehouse
Write-Host "Test 2: Create Warehouse" -ForegroundColor Yellow
$newWarehouse = @{
    code = "WH-TEST-001"
    name = "Test Warehouse 1"
    location = "New York, NY"
    description = "Test warehouse created via API"
} | ConvertTo-Json

try {
    $created = Invoke-RestMethod -Uri $baseUrl -Method POST -Headers $headers -Body $newWarehouse
    Write-Host "✓ Created Warehouse:" -ForegroundColor Green
    Write-Host "  ID: $($created.id)"
    Write-Host "  Code: $($created.code)"
    Write-Host "  Name: $($created.name)"
    $warehouseId = $created.id
} catch {
    Write-Host "✗ Create Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 3: Create Another Warehouse
Write-Host "Test 3: Create Second Warehouse" -ForegroundColor Yellow
$newWarehouse2 = @{
    code = "WH-TEST-002"
    name = "Test Warehouse 2"
    location = "Los Angeles, CA"
    description = "Second test warehouse"
} | ConvertTo-Json

try {
    $created2 = Invoke-RestMethod -Uri $baseUrl -Method POST -Headers $headers -Body $newWarehouse2
    Write-Host "✓ Created Second Warehouse: $($created2.name)" -ForegroundColor Green
} catch {
    Write-Host "✗ Create Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 4: Get All Warehouses
Write-Host "Test 4: Get All Warehouses" -ForegroundColor Yellow
try {
    $allWarehouses = Invoke-RestMethod -Uri $baseUrl -Method GET
    Write-Host "✓ Retrieved $($allWarehouses.Count) warehouses" -ForegroundColor Green
    foreach ($wh in $allWarehouses) {
        Write-Host "  - $($wh.code): $($wh.name) (Products: $($wh.totalProducts))" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ Get All Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 5: Get Warehouse by ID
Write-Host "Test 5: Get Warehouse by ID" -ForegroundColor Yellow
try {
    $warehouse = Invoke-RestMethod -Uri "$baseUrl/$warehouseId" -Method GET
    Write-Host "✓ Retrieved: $($warehouse.name)" -ForegroundColor Green
} catch {
    Write-Host "✗ Get by ID Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 6: Update Warehouse
Write-Host "Test 6: Update Warehouse" -ForegroundColor Yellow
$updateWarehouse = @{
    code = "WH-TEST-001"
    name = "Test Warehouse 1 - UPDATED"
    location = "New York, NY - Expanded"
    description = "Updated test warehouse"
} | ConvertTo-Json

try {
    $updated = Invoke-RestMethod -Uri "$baseUrl/$warehouseId" -Method PUT -Headers $headers -Body $updateWarehouse
    Write-Host "✓ Updated: $($updated.name)" -ForegroundColor Green
} catch {
    Write-Host "✗ Update Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 7: Search Warehouses
Write-Host "Test 7: Search Warehouses" -ForegroundColor Yellow
try {
    $searchResults = Invoke-RestMethod -Uri "$baseUrl/search?name=test" -Method GET
    Write-Host "✓ Found $($searchResults.Count) warehouses matching 'test'" -ForegroundColor Green
} catch {
    Write-Host "✗ Search Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 8: Get Dashboard
Write-Host "Test 8: Get Dashboard" -ForegroundColor Yellow
try {
    $dashboard = Invoke-RestMethod -Uri "$baseUrl/dashboard" -Method GET
    Write-Host "✓ Dashboard Retrieved:" -ForegroundColor Green
    Write-Host "  Total Warehouses: $($dashboard.totalWarehouses)" -ForegroundColor Cyan
    Write-Host "  Total Products: $($dashboard.totalProducts)" -ForegroundColor Cyan
    Write-Host "  Total Stock Quantity: $($dashboard.totalStockQuantity)" -ForegroundColor Cyan
    Write-Host "  Low Stock Items: $($dashboard.lowStockItems)" -ForegroundColor Yellow
    Write-Host "  Out of Stock Items: $($dashboard.outOfStockItems)" -ForegroundColor Red
} catch {
    Write-Host "✗ Dashboard Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 9: Get Warehouse Stats
Write-Host "Test 9: Get Warehouse Stats" -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "$baseUrl/$warehouseId/stats" -Method GET
    Write-Host "✓ Stats Retrieved:" -ForegroundColor Green
    Write-Host "  Product Count: $($stats.productCount)" -ForegroundColor Cyan
    Write-Host "  Total Quantity: $($stats.totalQuantity)" -ForegroundColor Cyan
    Write-Host "  Utilization: $($stats.utilizationPercentage)%" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Stats Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 10: Get Top Warehouses
Write-Host "Test 10: Get Top Warehouses" -ForegroundColor Yellow
try {
    $topWarehouses = Invoke-RestMethod -Uri "$baseUrl/top?limit=5" -Method GET
    Write-Host "✓ Top Warehouses:" -ForegroundColor Green
    foreach ($top in $topWarehouses) {
        Write-Host "  #$($top.rank): $($top.warehouseName) - $($top.totalQuantity) units" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ Top Warehouses Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 11: Get Low Stock Alerts
Write-Host "Test 11: Get Low Stock Alerts" -ForegroundColor Yellow
try {
    $alerts = Invoke-RestMethod -Uri "$baseUrl/alerts/low-stock" -Method GET
    Write-Host "✓ Found $($alerts.Count) stock alerts" -ForegroundColor Green
    foreach ($alert in $alerts) {
        $color = switch ($alert.alertLevel) {
            "OUT_OF_STOCK" { "Red" }
            "CRITICAL" { "Yellow" }
            "LOW" { "Cyan" }
            default { "Gray" }
        }
        Write-Host "  [$($alert.alertLevel)] $($alert.productName) at $($alert.warehouseName): $($alert.currentQuantity) units" -ForegroundColor $color
    }
} catch {
    Write-Host "✗ Alerts Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 12: Get Warehouse Details
Write-Host "Test 12: Get Warehouse Details" -ForegroundColor Yellow
try {
    $details = Invoke-RestMethod -Uri "$baseUrl/$warehouseId/details" -Method GET
    Write-Host "✓ Details Retrieved:" -ForegroundColor Green
    Write-Host "  Warehouse: $($details.name)" -ForegroundColor Cyan
    Write-Host "  Total Products: $($details.totalProducts)" -ForegroundColor Cyan
    Write-Host "  Inventories: $($details.inventories.Count)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ Details Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 13: Get Warehouse Inventory
Write-Host "Test 13: Get Warehouse Inventory" -ForegroundColor Yellow
try {
    $inventory = Invoke-RestMethod -Uri "$baseUrl/$warehouseId/inventory" -Method GET
    Write-Host "✓ Retrieved $($inventory.Count) inventory items" -ForegroundColor Green
} catch {
    Write-Host "✗ Inventory Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 14: Test Validation Error (Empty Code)
Write-Host "Test 14: Test Validation (should fail)" -ForegroundColor Yellow
$invalidWarehouse = @{
    code = ""
    name = "Invalid"
    location = "Test"
} | ConvertTo-Json

try {
    $invalid = Invoke-RestMethod -Uri $baseUrl -Method POST -Headers $headers -Body $invalidWarehouse
    Write-Host "✗ Validation should have failed!" -ForegroundColor Red
} catch {
    Write-Host "✓ Validation error caught correctly" -ForegroundColor Green
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Gray
}
Write-Host ""

# Test 15: Test Duplicate Code (should fail)
Write-Host "Test 15: Test Duplicate Code (should fail)" -ForegroundColor Yellow
$duplicateWarehouse = @{
    code = "WH-TEST-001"
    name = "Duplicate Test"
    location = "Test"
} | ConvertTo-Json

try {
    $duplicate = Invoke-RestMethod -Uri $baseUrl -Method POST -Headers $headers -Body $duplicateWarehouse
    Write-Host "✗ Duplicate check should have failed!" -ForegroundColor Red
} catch {
    Write-Host "✓ Duplicate code error caught correctly" -ForegroundColor Green
    Write-Host "  Error: Already exists" -ForegroundColor Gray
}
Write-Host ""

# Test 16: Delete Warehouse
Write-Host "Test 16: Delete Warehouse" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$baseUrl/$warehouseId" -Method DELETE
    Write-Host "✓ Warehouse deleted successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Delete Failed: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Testing Complete!" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "API Documentation: WAREHOUSE-API-EXAMPLES.md" -ForegroundColor Yellow
Write-Host "Implementation Guide: WAREHOUSE-IMPLEMENTATION.md" -ForegroundColor Yellow
Write-Host "Quick Summary: WAREHOUSE-QUICK-SUMMARY.md" -ForegroundColor Yellow
