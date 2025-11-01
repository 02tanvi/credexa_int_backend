# Test Product Pricing Service API

## Quick Test

### 1. Test if service is running
```bash
curl http://localhost:8084/api/products/swagger-ui.html
```
Should open Swagger UI in browser.

### 2. Test Get Product by Code (The failing endpoint)
```bash
curl -X GET "http://localhost:8084/api/products/code/FD-STD-6M" -H "accept: application/json"
```

**Expected:** 200 OK with product data wrapped in ApiResponse

**If you get 500:** Check Product Pricing Service console logs for the actual error.

### 3. Test Get All Products
```bash
curl -X GET "http://localhost:8084/api/products?page=0&size=10" -H "accept: application/json"
```

### 4. Test Get Product by ID
```bash
curl -X GET "http://localhost:8084/api/products/8" -H "accept: application/json"
```

## Common Issues

### Issue 1: Product Not Found
If product doesn't exist, should return 404, not 500.

### Issue 2: Database Connection
Check Product Pricing Service can connect to `product_db` database.

### Issue 3: Lazy Loading Issue
The ProductResponse might be trying to load related entities (interestRateMatrix, charges, etc.) but session is closed.

## Debug Steps

1. **Check Product Pricing Service logs** - Look for stack trace around 15:50:34
2. **Test in Swagger UI** - Go to http://localhost:8084/api/products/swagger-ui.html and try GET /products/code/{code} with "FD-STD-6M"
3. **Check database** - Verify product exists:
   ```sql
   SELECT * FROM products WHERE product_code = 'FD-STD-6M';
   ```

## Likely Issue

The Product Pricing Service is probably having a **lazy initialization exception** when trying to load the related entities (interest rate matrix, charges, roles, etc.) because the controller is returning the entity directly instead of a detached DTO.

Check if the ProductService is properly mapping the Product entity to ProductResponse DTO.
