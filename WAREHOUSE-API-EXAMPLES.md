# Warehouse Management API - Complete Guide with JSON Examples

## Base URL
```
http://localhost:8080/api/warehouses
```

---

## 📋 Table of Contents
1. [CRUD Operations](#crud-operations)
2. [Search & Query](#search--query)
3. [Analytics & Dashboard](#analytics--dashboard)
4. [Inventory Management](#inventory-management)
5. [Alerts & Monitoring](#alerts--monitoring)

---

## CRUD Operations

### 1. Create Warehouse
**Endpoint:** `POST /api/warehouses`

**Request Headers:**
```json
{
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "code": "WH001",
  "name": "Main Distribution Center",
  "location": "New York, NY 10001",
  "description": "Primary warehouse for East Coast operations"
}
```

**Success Response:** `201 Created`
```json
{
  "id": 1,
  "code": "WH001",
  "name": "Main Distribution Center",
  "location": "New York, NY 10001",
  "description": "Primary warehouse for East Coast operations",
  "totalProducts": 0,
  "totalQuantity": 0
}
```

**Error Response:** `400 Bad Request`
```json
{
  "timestamp": "2026-01-16T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Warehouse with code 'WH001' already exists",
  "path": "/api/warehouses"
}
```

**Validation Errors:**
```json
{
  "timestamp": "2026-01-16T10:30:00.000+00:00",
  "status": 400,
  "errors": {
    "code": "Code must be between 2 and 20 characters",
    "name": "Warehouse name is required"
  }
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/warehouses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WH001",
    "name": "Main Distribution Center",
    "location": "New York, NY 10001",
    "description": "Primary warehouse for East Coast operations"
  }'
```

---

### 2. Get All Warehouses
**Endpoint:** `GET /api/warehouses`

**Request:** No body required

**Success Response:** `200 OK`
```json
[
  {
    "id": 1,
    "code": "WH001",
    "name": "Main Distribution Center",
    "location": "New York, NY 10001",
    "description": "Primary warehouse for East Coast operations",
    "totalProducts": 150,
    "totalQuantity": 5420
  },
  {
    "id": 2,
    "code": "WH002",
    "name": "West Coast Warehouse",
    "location": "Los Angeles, CA 90001",
    "description": "Secondary warehouse for West Coast",
    "totalProducts": 120,
    "totalQuantity": 3890
  },
  {
    "id": 3,
    "code": "WH003",
    "name": "Central Hub",
    "location": "Chicago, IL 60601",
    "description": "Central distribution hub",
    "totalProducts": 200,
    "totalQuantity": 7850
  }
]
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses
```

---

### 3. Get Warehouse by ID
**Endpoint:** `GET /api/warehouses/{id}`

**Example:** `GET /api/warehouses/1`

**Success Response:** `200 OK`
```json
{
  "id": 1,
  "code": "WH001",
  "name": "Main Distribution Center",
  "location": "New York, NY 10001",
  "description": "Primary warehouse for East Coast operations",
  "totalProducts": 150,
  "totalQuantity": 5420
}
```

**Error Response:** `404 Not Found`
```json
{
  "timestamp": "2026-01-16T10:30:00.000+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Warehouse not found with id: 999",
  "path": "/api/warehouses/999"
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/1
```

---

### 4. Update Warehouse
**Endpoint:** `PUT /api/warehouses/{id}`

**Example:** `PUT /api/warehouses/1`

**Request Body:**
```json
{
  "code": "WH001",
  "name": "Main Distribution Center - Updated",
  "location": "New York, NY 10001",
  "description": "Updated description - Primary warehouse with expanded capacity"
}
```

**Success Response:** `200 OK`
```json
{
  "id": 1,
  "code": "WH001",
  "name": "Main Distribution Center - Updated",
  "location": "New York, NY 10001",
  "description": "Updated description - Primary warehouse with expanded capacity",
  "totalProducts": 150,
  "totalQuantity": 5420
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/warehouses/1 \
  -H "Content-Type: application/json" \
  -d '{
    "code": "WH001",
    "name": "Main Distribution Center - Updated",
    "location": "New York, NY 10001",
    "description": "Updated description"
  }'
```

---

### 5. Delete Warehouse
**Endpoint:** `DELETE /api/warehouses/{id}`

**Example:** `DELETE /api/warehouses/1`

**Success Response:** `204 No Content`
(No body returned)

**Error Response - Has Inventory:** `400 Bad Request`
```json
{
  "timestamp": "2026-01-16T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot delete warehouse with existing inventory. Please transfer or remove all inventory first.",
  "path": "/api/warehouses/1"
}
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/warehouses/1
```

---

## Search & Query

### 6. Search Warehouses by Name
**Endpoint:** `GET /api/warehouses/search?name={searchTerm}`

**Example:** `GET /api/warehouses/search?name=main`

**Success Response:** `200 OK`
```json
[
  {
    "id": 1,
    "code": "WH001",
    "name": "Main Distribution Center",
    "location": "New York, NY 10001",
    "description": "Primary warehouse for East Coast operations",
    "totalProducts": 150,
    "totalQuantity": 5420
  }
]
```

**cURL Example:**
```bash
curl "http://localhost:8080/api/warehouses/search?name=main"
```

---

## Analytics & Dashboard

### 7. Get Complete Dashboard
**Endpoint:** `GET /api/warehouses/dashboard`

**Success Response:** `200 OK`
```json
{
  "totalWarehouses": 3,
  "totalProducts": 470,
  "totalInventoryValue": 0,
  "totalStockQuantity": 17160,
  "lowStockItems": 12,
  "outOfStockItems": 3,
  "warehouseStats": [
    {
      "warehouseId": 1,
      "warehouseName": "Main Distribution Center",
      "warehouseCode": "WH001",
      "location": "New York, NY 10001",
      "productCount": 150,
      "totalQuantity": 5420,
      "availableQuantity": 4890,
      "reservedQuantity": 530,
      "utilizationPercentage": 54.2
    },
    {
      "warehouseId": 2,
      "warehouseName": "West Coast Warehouse",
      "warehouseCode": "WH002",
      "location": "Los Angeles, CA 90001",
      "productCount": 120,
      "totalQuantity": 3890,
      "availableQuantity": 3450,
      "reservedQuantity": 440,
      "utilizationPercentage": 38.9
    },
    {
      "warehouseId": 3,
      "warehouseName": "Central Hub",
      "warehouseCode": "WH003",
      "location": "Chicago, IL 60601",
      "productCount": 200,
      "totalQuantity": 7850,
      "availableQuantity": 7100,
      "reservedQuantity": 750,
      "utilizationPercentage": 78.5
    }
  ],
  "topWarehousesByInventory": [
    {
      "warehouseId": 3,
      "warehouseName": "Central Hub",
      "warehouseCode": "WH003",
      "totalProducts": 200,
      "totalQuantity": 7850,
      "rank": 1
    },
    {
      "warehouseId": 1,
      "warehouseName": "Main Distribution Center",
      "warehouseCode": "WH001",
      "totalProducts": 150,
      "totalQuantity": 5420,
      "rank": 2
    },
    {
      "warehouseId": 2,
      "warehouseName": "West Coast Warehouse",
      "warehouseCode": "WH002",
      "totalProducts": 120,
      "totalQuantity": 3890,
      "rank": 3
    }
  ],
  "lowStockAlerts": [
    {
      "warehouseId": 1,
      "warehouseName": "Main Distribution Center",
      "productId": 45,
      "productName": "Laptop Model X",
      "productSku": "LAP-001",
      "currentQuantity": 0,
      "reorderLevel": 10,
      "alertLevel": "OUT_OF_STOCK"
    },
    {
      "warehouseId": 2,
      "warehouseName": "West Coast Warehouse",
      "productId": 67,
      "productName": "Monitor 24 inch",
      "productSku": "MON-024",
      "currentQuantity": 3,
      "reorderLevel": 10,
      "alertLevel": "CRITICAL"
    },
    {
      "warehouseId": 1,
      "warehouseName": "Main Distribution Center",
      "productId": 89,
      "productName": "Wireless Mouse",
      "productSku": "MOU-001",
      "currentQuantity": 8,
      "reorderLevel": 10,
      "alertLevel": "LOW"
    }
  ]
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/dashboard
```

---

### 8. Get Warehouse Statistics
**Endpoint:** `GET /api/warehouses/{id}/stats`

**Example:** `GET /api/warehouses/1/stats`

**Success Response:** `200 OK`
```json
{
  "warehouseId": 1,
  "warehouseName": "Main Distribution Center",
  "warehouseCode": "WH001",
  "location": "New York, NY 10001",
  "productCount": 150,
  "totalQuantity": 5420,
  "availableQuantity": 4890,
  "reservedQuantity": 530,
  "utilizationPercentage": 54.2
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/1/stats
```

---

### 9. Get Warehouse Details with Inventory
**Endpoint:** `GET /api/warehouses/{id}/details`

**Example:** `GET /api/warehouses/1/details`

**Success Response:** `200 OK`
```json
{
  "id": 1,
  "code": "WH001",
  "name": "Main Distribution Center",
  "location": "New York, NY 10001",
  "description": "Primary warehouse for East Coast operations",
  "totalProducts": 3,
  "totalQuantity": 450,
  "inventories": [
    {
      "inventoryId": 101,
      "product": {
        "id": 45,
        "name": "Laptop Model X",
        "sku": "LAP-001",
        "category": "Electronics",
        "price": 999.99
      },
      "quantityOnHand": 150,
      "quantityReserved": 20,
      "availableQuantity": 130
    },
    {
      "inventoryId": 102,
      "product": {
        "id": 67,
        "name": "Monitor 24 inch",
        "sku": "MON-024",
        "category": "Electronics",
        "price": 299.99
      },
      "quantityOnHand": 200,
      "quantityReserved": 35,
      "availableQuantity": 165
    },
    {
      "inventoryId": 103,
      "product": {
        "id": 89,
        "name": "Wireless Mouse",
        "sku": "MOU-001",
        "category": "Accessories",
        "price": 29.99
      },
      "quantityOnHand": 100,
      "quantityReserved": 15,
      "availableQuantity": 85
    }
  ]
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/1/details
```

---

### 10. Get Top Warehouses
**Endpoint:** `GET /api/warehouses/top?limit={number}`

**Example:** `GET /api/warehouses/top?limit=3`

**Success Response:** `200 OK`
```json
[
  {
    "warehouseId": 3,
    "warehouseName": "Central Hub",
    "warehouseCode": "WH003",
    "totalProducts": 200,
    "totalQuantity": 7850,
    "rank": 1
  },
  {
    "warehouseId": 1,
    "warehouseName": "Main Distribution Center",
    "warehouseCode": "WH001",
    "totalProducts": 150,
    "totalQuantity": 5420,
    "rank": 2
  },
  {
    "warehouseId": 2,
    "warehouseName": "West Coast Warehouse",
    "warehouseCode": "WH002",
    "totalProducts": 120,
    "totalQuantity": 3890,
    "rank": 3
  }
]
```

**cURL Example:**
```bash
curl "http://localhost:8080/api/warehouses/top?limit=3"
```

---

### 11. Get Warehouse Stock Summary
**Endpoint:** `GET /api/warehouses/{id}/summary`

**Example:** `GET /api/warehouses/1/summary`

**Success Response:** `200 OK`
```json
{
  "id": 1,
  "code": "WH001",
  "name": "Main Distribution Center",
  "location": "New York, NY 10001",
  "description": "Primary warehouse for East Coast operations",
  "totalProducts": 150,
  "totalQuantity": 5420
}
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/1/summary
```

---

## Inventory Management

### 12. Get Warehouse Inventory
**Endpoint:** `GET /api/warehouses/{id}/inventory`

**Example:** `GET /api/warehouses/1/inventory`

**Success Response:** `200 OK`
```json
[
  {
    "inventoryId": 101,
    "product": {
      "id": 45,
      "name": "Laptop Model X",
      "sku": "LAP-001",
      "category": "Electronics",
      "price": 999.99
    },
    "quantityOnHand": 150,
    "quantityReserved": 20,
    "availableQuantity": 130
  },
  {
    "inventoryId": 102,
    "product": {
      "id": 67,
      "name": "Monitor 24 inch",
      "sku": "MON-024",
      "category": "Electronics",
      "price": 299.99
    },
    "quantityOnHand": 200,
    "quantityReserved": 35,
    "availableQuantity": 165
  },
  {
    "inventoryId": 103,
    "product": {
      "id": 89,
      "name": "Wireless Mouse",
      "sku": "MOU-001",
      "category": "Accessories",
      "price": 29.99
    },
    "quantityOnHand": 100,
    "quantityReserved": 15,
    "availableQuantity": 85
  }
]
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/1/inventory
```

---

### 13. Get Warehouse Inventory for Specific Product
**Endpoint:** `GET /api/warehouses/{warehouseId}/products/{productId}/inventory`

**Example:** `GET /api/warehouses/1/products/45/inventory`

**Success Response:** `200 OK`
```json
[
  {
    "inventoryId": 101,
    "product": {
      "id": 45,
      "name": "Laptop Model X",
      "sku": "LAP-001",
      "category": "Electronics",
      "price": 999.99
    },
    "quantityOnHand": 150,
    "quantityReserved": 20,
    "availableQuantity": 130
  }
]
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/1/products/45/inventory
```

---

## Alerts & Monitoring

### 14. Get Low Stock Alerts
**Endpoint:** `GET /api/warehouses/alerts/low-stock`

**Success Response:** `200 OK`
```json
[
  {
    "warehouseId": 1,
    "warehouseName": "Main Distribution Center",
    "productId": 45,
    "productName": "Laptop Model X",
    "productSku": "LAP-001",
    "currentQuantity": 0,
    "reorderLevel": 10,
    "alertLevel": "OUT_OF_STOCK"
  },
  {
    "warehouseId": 2,
    "warehouseName": "West Coast Warehouse",
    "productId": 67,
    "productName": "Monitor 24 inch",
    "productSku": "MON-024",
    "currentQuantity": 3,
    "reorderLevel": 10,
    "alertLevel": "CRITICAL"
  },
  {
    "warehouseId": 1,
    "warehouseName": "Main Distribution Center",
    "productId": 89,
    "productName": "Wireless Mouse",
    "productSku": "MOU-001",
    "currentQuantity": 8,
    "reorderLevel": 10,
    "alertLevel": "LOW"
  },
  {
    "warehouseId": 3,
    "warehouseName": "Central Hub",
    "productId": 112,
    "productName": "Keyboard Mechanical",
    "productSku": "KEY-001",
    "currentQuantity": 5,
    "reorderLevel": 10,
    "alertLevel": "CRITICAL"
  }
]
```

**Alert Levels:**
- `OUT_OF_STOCK`: currentQuantity = 0
- `CRITICAL`: currentQuantity < (reorderLevel / 2)
- `LOW`: currentQuantity <= reorderLevel

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/alerts/low-stock
```

---

### 15. Health Check
**Endpoint:** `GET /api/warehouses/health`

**Success Response:** `200 OK`
```
Warehouse Service is running
```

**cURL Example:**
```bash
curl http://localhost:8080/api/warehouses/health
```

---

## Postman Collection Example

Here's a complete Postman collection JSON you can import:

```json
{
  "info": {
    "name": "Warehouse Management API",
    "description": "Complete API collection for warehouse management",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create Warehouse",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"code\": \"WH001\",\n  \"name\": \"Main Distribution Center\",\n  \"location\": \"New York, NY 10001\",\n  \"description\": \"Primary warehouse\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/warehouses",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "warehouses"]
        }
      }
    },
    {
      "name": "Get All Warehouses",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/warehouses",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "warehouses"]
        }
      }
    },
    {
      "name": "Get Dashboard",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/warehouses/dashboard",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "warehouses", "dashboard"]
        }
      }
    },
    {
      "name": "Get Low Stock Alerts",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/warehouses/alerts/low-stock",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "warehouses", "alerts", "low-stock"]
        }
      }
    }
  ]
}
```

---

## Error Handling

All endpoints follow standard HTTP status codes:

### Success Responses
- `200 OK` - Successful GET/PUT request
- `201 Created` - Successful POST request
- `204 No Content` - Successful DELETE request

### Error Responses
- `400 Bad Request` - Validation errors or business logic violations
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

### Error Response Format
```json
{
  "timestamp": "2026-01-16T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/warehouses/1"
}
```

---

## Testing Script (PowerShell)

```powershell
# Create a warehouse
$createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/warehouses" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"code":"WH001","name":"Test Warehouse","location":"NYC","description":"Test"}'

Write-Host "Created warehouse:" $createResponse

# Get all warehouses
$allWarehouses = Invoke-RestMethod -Uri "http://localhost:8080/api/warehouses" -Method GET
Write-Host "Total warehouses:" $allWarehouses.Count

# Get dashboard
$dashboard = Invoke-RestMethod -Uri "http://localhost:8080/api/warehouses/dashboard" -Method GET
Write-Host "Total products:" $dashboard.totalProducts
Write-Host "Total stock:" $dashboard.totalStockQuantity
```

---

## Quick Reference

| Operation | Method | Endpoint | Body Required |
|-----------|--------|----------|---------------|
| Create | POST | `/api/warehouses` | ✅ |
| Get All | GET | `/api/warehouses` | ❌ |
| Get One | GET | `/api/warehouses/{id}` | ❌ |
| Update | PUT | `/api/warehouses/{id}` | ✅ |
| Delete | DELETE | `/api/warehouses/{id}` | ❌ |
| Search | GET | `/api/warehouses/search?name={name}` | ❌ |
| Dashboard | GET | `/api/warehouses/dashboard` | ❌ |
| Stats | GET | `/api/warehouses/{id}/stats` | ❌ |
| Alerts | GET | `/api/warehouses/alerts/low-stock` | ❌ |
| Inventory | GET | `/api/warehouses/{id}/inventory` | ❌ |

---

**Need help?** Check the complete implementation guide in `WAREHOUSE-IMPLEMENTATION.md`
