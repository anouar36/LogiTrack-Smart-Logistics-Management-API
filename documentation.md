# Product API Documentation

## ✅ Get All Products
GET /api/products

Example:
curl -X GET http://localhost:8080/api/products

---

## ✅ Get All Active Products
GET /api/products/active

Example:
curl -X GET http://localhost:8080/api/products/active

---

## ✅ Get Product by ID
GET /api/products/{id}

Example:
curl -X GET http://localhost:8080/api/products/5

---

## ✅ Search by Name
GET /api/products/name/{name}

Example:
curl -X GET http://localhost:8080/api/products/name/Laptop

---

## ✅ Get Products by Category
GET /api/products/category/{category}

Example:
curl -X GET http://localhost:8080/api/products/category/Electronics

---

## ✅ Price > X
GET /api/products/price/greater/{price}

Example:
curl -X GET http://localhost:8080/api/products/price/greater/1000

---

## ✅ Price < X
GET /api/products/price/less/{price}

Example:
curl -X GET http://localhost:8080/api/products/price/less/200

---

## ✅ Price Between
GET /api/products/price/between?min=100&max=500

Example:
curl -X GET "http://localhost:8080/api/products/price/between?min=100&max=500"

---

## ✅ Name Contains Keyword
GET /api/products/search?keyword=lap

Example:
curl -X GET "http://localhost:8080/api/products/search?keyword=lap"

---

## ✅ Category AND Price < X
GET /api/products/category/{category}/price/less/{price}

Example:
curl -X GET http://localhost:8080/api/products/category/Electronics/price/less/2000

---

## ✅ Add Product (DTO)
POST /api/products  
Body:
{
"name": "Keyboard RGB",
"sku": "KEY900",
"price": 300,
"category": "Accessories"
}

Example:
curl -X POST http://localhost:8080/api/products -H "Content-Type: application/json" -d "{\"name\":\"Keyboard RGB\",\"sku\":\"KEY900\",\"price\":300,\"category\":\"Accessories\"}"

---

## ✅ Add Product (Entity)
POST /api/products/entity  
Body:
{
"name": "SSD Kingston",
"sku": "SSD88",
"price": 550,
"category": "Storage"
}

---

## ✅ Add Many Products
POST /api/products/bulk  
Body:
[
{
"name": "Monitor 27",
"sku": "MON27",
"price": 1500,
"category": "Displays"
},
{
"name": "GPU RTX",
"sku": "RTX444",
"price": 9000,
"category": "Graphics"
}
]

---

## ✅ Update Product by ID
PUT /api/products/{id}  
Body:
    {
    "name": "Mouse Wireless",
    "sku": "MOU900",
    "price": 250,
    "category": "Accessories"
    }

Example:
curl -X PUT http://localhost:8080/api/products/4 -H "Content-Type: application/json" -d "{\"name\":\"Mouse Wireless\",\"sku\":\"MOU900\",\"price\":250,\"category\":\"Accessories\"}"

---

## ✅ Delete Product by ID
DELETE /api/products/{id}

Example:
curl -X DELETE http://localhost:8080/api/products/4

---

## ✅ Delete Product (Entity)+++++++++++++++++++++++++++++++++++++++
DELETE /api/products  
Body:
{
"id": 4
}

---

## ✅ Delete All Products
    DELETE /api/products/all

Example:
curl -X DELETE http://localhost:8080/api/products/all

---

## ✅ Soft Delete
DELETE /api/products/soft/{id}

Example:
curl -X DELETE http://localhost:8080/api/products/soft/5

---

## ✅ Count Products
GET /api/products/count

Example:
curl -X GET http://localhost:8080/api/products/count
