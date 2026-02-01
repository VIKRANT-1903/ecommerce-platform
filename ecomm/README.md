# Ecomm – E-commerce Backend

Spring Boot backend for an e-commerce application. Handles carts, inventory (with Redis cache and locking), orders, checkout orchestration, and email notifications.

## Tech Stack

- **Java 17**
- **Spring Boot 4.x**
- **PostgreSQL** – primary data store
- **Redis** – inventory cache and distributed locks
- **Lombok** – boilerplate reduction
- **JPA / Hibernate** – persistence

## Prerequisites

- JDK 17+
- PostgreSQL (e.g. Neon)
- Redis (e.g. Redis Labs)
- Gradle (or use wrapper)

## Getting Started

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

Server runs on **http://localhost:8080** by default.

### Configuration

Edit `src/main/resources/application.properties` (or use env vars):

- **PostgreSQL:** `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
- **Redis:** `spring.data.redis.host`, `spring.data.redis.port`, `spring.data.redis.password`, `spring.data.redis.username`

## API Response Format

All API responses are wrapped in a common structure:

```json
{
  "success": true,
  "data": { ... },
  "message": null,
  "errorCode": null
}
```

- **success** – `true` if the request succeeded, `false` on business/validation failure
- **data** – response payload (null on failure when not provided)
- **message** – human-readable message (often set on failure)
- **errorCode** – machine-readable error code (e.g. `RESOURCE_NOT_FOUND`, `CHECKOUT_FAILED`)

HTTP status codes:

- **200** – Success
- **201** – Created
- **400** – Bad Request (e.g. validation, empty cart)
- **404** – Not Found
- **409** – Conflict (e.g. lock acquisition failed)
- **422** – Unprocessable Entity (e.g. insufficient inventory, checkout failed)
- **500** – Internal Server Error

---

## API Endpoints

### Cart

Base path: **`/api/users/{userId}/cart`**  
`userId` is an integer.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/users/{userId}/cart` | Get or create active cart |
| GET | `/api/users/{userId}/cart/view` | Get active cart (404 if none) |
| POST | `/api/users/{userId}/cart/items` | Add item to cart |
| PATCH | `/api/users/{userId}/cart/items/{cartItemId}` | Update item quantity |
| DELETE | `/api/users/{userId}/cart/items/{cartItemId}` | Remove item from cart |

#### GET `/api/users/{userId}/cart`

Returns the user’s active cart, creating an empty one if none exists.

**Response (200):**

```json
{
  "success": true,
  "data": {
    "cartId": 1,
    "userId": 17,
    "status": "ACTIVE",
    "updatedAt": "2025-01-30T12:00:00Z",
    "items": [
      {
        "cartItemId": 1,
        "productId": "p1",
        "merchantId": 1,
        "quantity": 1,
        "priceSnapshot": 9.99
      }
    ]
  },
  "message": null,
  "errorCode": null
}
```

#### GET `/api/users/{userId}/cart/view`

Returns the user’s active cart. Fails with 404 if no active cart exists.

**Response (200):** Same shape as GET `/api/users/{userId}/cart`.  
**Response (404):** `success: false`, `errorCode: "RESOURCE_NOT_FOUND"`.

#### POST `/api/users/{userId}/cart/items`

Adds an item to the cart.

**Request body:**

```json
{
  "productId": "p1",
  "merchantId": 1,
  "quantity": 1,
  "priceSnapshot": 9.99
}
```

**Response (201):** `data` is the full cart (same shape as GET cart).

#### PATCH `/api/users/{userId}/cart/items/{cartItemId}`

Updates the quantity of a cart item.

**Request body:**

```json
{
  "quantity": 2
}
```

**Response (200):** Full cart.  
**Response (404):** If cart or cart item not found.

#### DELETE `/api/users/{userId}/cart/items/{cartItemId}`

Removes an item from the cart.

**Response (200):** Full cart.  
**Response (404):** If cart or cart item not found.

---

### Order

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/users/{userId}/orders` | Create order from cart |
| GET | `/api/orders/{orderId}` | Get order by ID |

#### POST `/api/users/{userId}/orders`

Creates an order from the user’s current cart. Cart must have at least one item.

**Request body:**

```json
{
  "shippingAddress": "123 Main St"
}
```

**Response (201):**

```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "userId": 17,
    "totalAmount": 9.99,
    "orderStatus": "CREATED",
    "paymentStatus": "PENDING",
    "shippingAddress": "123 Main St",
    "createdAt": "2025-01-30T12:00:00Z",
    "items": [
      {
        "orderItemId": 1,
        "productId": "p1",
        "merchantId": 1,
        "quantity": 1,
        "price": 9.99
      }
    ]
  },
  "message": null,
  "errorCode": null
}
```

**Response (400):** Cart empty – `success: false`, `message` e.g. "Cart is empty".  
**Response (404):** No active cart.

#### GET `/api/orders/{orderId}`

Returns order details by ID.

**Response (200):** Same `data` shape as POST create order.  
**Response (404):** Order not found.

---

### Inventory

Base path: **`/api/inventory`**. Used by checkout; can also be called directly for reads and reserve/confirm/release.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/inventory?productId={id}&merchantId={id}` | Fetch inventory (cache then DB) |
| POST | `/api/inventory/reserve` | Reserve quantity for checkout |
| POST | `/api/inventory/confirm` | Confirm after payment success |
| POST | `/api/inventory/release` | Release reserved on payment failure |

#### GET `/api/inventory`

**Query params:** `productId` (string), `merchantId` (integer).

**Response (200):**

```json
{
  "success": true,
  "data": {
    "inventoryId": 1,
    "productId": "p1",
    "merchantId": 1,
    "availableQty": 100,
    "reservedQty": 0,
    "updatedAt": "2025-01-30T12:00:00Z"
  },
  "message": null,
  "errorCode": null
}
```

**Response (404):** Inventory not found for product/merchant.

#### POST `/api/inventory/reserve`

**Request body:**

```json
{
  "productId": "p1",
  "merchantId": 1,
  "quantity": 2
}
```

**Response (200):**

```json
{
  "success": true,
  "data": {
    "success": true,
    "message": "Reserved"
  }
}
```

On insufficient inventory, `data.success` is `false` and `data.message` describes the failure.  
**Response (404):** Inventory not found.  
**Response (409):** Lock acquisition failed (`LOCK_ACQUISITION_FAILED`).

#### POST `/api/inventory/confirm`

**Request body:** Same as reserve (`productId`, `merchantId`, `quantity`).

**Response (200):** `data: null`, `message: "Confirmed"`.  
**Response (404/422):** Not found or insufficient reserved.

#### POST `/api/inventory/release`

**Request body:** Same as reserve.

**Response (200):** `data: null`, `message: "Released"`.

---

### Checkout

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/users/{userId}/checkout` | Run full checkout (cart → order → reserve → payment → confirm/release → clear cart → email) |

#### POST `/api/users/{userId}/checkout`

Orchestrates: fetch cart → create order → reserve inventory → dummy payment → on success: confirm inventory, mark order PAID, clear cart, send email; on failure: release inventory, mark order FAILED, clear cart, send failure email.

**Request body:**

```json
{
  "shippingAddress": "123 Main St"
}
```

**Response (200) – success:**

```json
{
  "success": true,
  "data": {
    "success": true,
    "orderId": 1,
    "message": "Checkout completed successfully",
    "order": {
      "orderId": 1,
      "userId": 17,
      "totalAmount": 9.99,
      "orderStatus": "PAID",
      "paymentStatus": "PAID",
      "shippingAddress": "123 Main St",
      "createdAt": "2025-01-30T12:00:00Z",
      "items": [ ... ]
    }
  },
  "message": null,
  "errorCode": null
}
```

**Response (422) – failure:**

```json
{
  "success": false,
  "data": {
    "success": false,
    "orderId": 1,
    "message": "Insufficient inventory: available=0, requested=1",
    "order": null
  },
  "message": "Insufficient inventory: ...",
  "errorCode": "CHECKOUT_FAILED"
}
```

Common failure messages: cart empty, inventory not found, insufficient inventory, payment failed (dummy gateway is random), confirm failed.

---

## Database Tables

The application expects these tables (same DB):

- **users** – id, email, first_name, last_name, password_hash, phone, role, created_at
- **carts** – cart_id, user_id, status, updated_at
- **cart_items** – cart_item_id, cart_id, product_id, merchant_id, quantity, price_snapshot
- **inventory** – inventory_id, product_id, merchant_id, available_qty, reserved_qty, updated_at
- **orders** – order_id, user_id, total_amount, order_status, payment_status, shipping_address, created_at
- **order_items** – order_item_id, order_id, product_id, merchant_id, quantity, price

Ensure inventory rows exist for each product/merchant used in cart/checkout.

---

## Project Structure

```
com.example.ecomm
├── common          – ApiResponse, exceptions, global handler, config (Jackson, Async)
├── cart            – Cart & cart items (controller, service, repository, entity, dto)
├── order           – Orders & order items (controller, service, repository, entity, dto)
├── inventory       – Inventory (controller, service, repository, entity, dto; Redis cache & lock)
├── checkout        – Checkout orchestration (controller, service, dto; payment gateway, flow)
├── email           – Email service (async, dummy logging; fetches user email from users table)
└── user            – User entity & repository (for email resolution)
```

---

## Example: Cart → Checkout

```bash
# 1. Get or create cart for user 17
curl -s http://localhost:8080/api/users/17/cart

# 2. Add product p1, merchant 1, quantity 1
curl -s -X POST http://localhost:8080/api/users/17/cart/items \
  -H "Content-Type: application/json" \
  -d '{"productId":"p1","merchantId":1,"quantity":1,"priceSnapshot":9.99}'

# 3. Checkout (requires inventory row for p1 / merchant 1, user 17 in users table)
curl -s -X POST http://localhost:8080/api/users/17/checkout \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress": "123 Main St"}'
```

---

## License

Internal use / as specified by your organization.
