# Search Service API Contract

**Version:** 1.0  
**Base URL:** `http://localhost:8083`  
**Service:** Search Service

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Endpoints](#api-endpoints)
    - [Product Search](#product-search)
    - [Offer Ranking](#offer-ranking)
    - [Autocomplete/Suggestions](#autocompletesuggestions)
    - [Health & Admin](#health--admin)
4. [Data Models](#data-models)
5. [Error Handling](#error-handling)
6. [Rate Limiting](#rate-limiting)

---

## Overview

The Search Service provides high-performance product search, filtering, and ranking capabilities powered by Elasticsearch. It supports:

- Full-text search across product catalog
- Advanced filtering (price, category, ratings, stock)
- Category browsing
- Offer ranking for product detail pages
- Autocomplete/typeahead suggestions

### Key Features

- **Real-time indexing** via CDC (Change Data Capture)
- **Multi-field search** with boost weights
- **Faceted navigation** (coming soon)
- **Offer ranking** using weighted scoring algorithm
- **Autocomplete** with completion suggester

---

## Authentication

Currently, the API is open (no authentication required).

**Future:** Consider adding:
- API Key authentication
- OAuth 2.0 for user-specific features
- Rate limiting per API key

---

## API Endpoints

### Product Search

#### 1. Full-Text Product Search

Search products by keyword across name, description, and USP fields.

**Endpoint:** `GET /search/products`

**Query Parameters:**

| Parameter   | Type    | Required | Default | Description                          |
|-------------|---------|----------|---------|--------------------------------------|
| `q`         | string  | No       | -       | Search query (keywords)              |
| `page`      | integer | No       | 0       | Page number (0-indexed)              |
| `size`      | integer | No       | 20      | Results per page (max: 100)          |
| `sortBy`    | string  | No       | -       | Sort field: `price`, `rating`, `popularity` |
| `sortOrder` | string  | No       | -       | Sort order: `asc`, `desc`            |

**Request Example:**

```http
GET /search/products?q=iphone&page=0&size=20&sortBy=price&sortOrder=asc
```

**Response:** [ProductSearchResponseDTO](#productsearchresponsedto)

**Response Example:**

```json
{
  "products": [
    {
      "productId": "p123",
      "name": "iPhone 15 Pro Max",
      "categoryName": "Smartphones",
      "minPrice": 129900.0,
      "merchantCount": 5,
      "inStock": true,
      "avgRating": 4.7,
      "images": [
        "https://cdn.example.com/iphone15-1.jpg",
        "https://cdn.example.com/iphone15-2.jpg"
      ]
    },
    {
      "productId": "p124",
      "name": "iPhone 14 Pro",
      "categoryName": "Smartphones",
      "minPrice": 99900.0,
      "merchantCount": 8,
      "inStock": true,
      "avgRating": 4.6,
      "images": [
        "https://cdn.example.com/iphone14-1.jpg"
      ]
    }
  ],
  "totalHits": 42,
  "page": 0,
  "size": 20,
  "facets": null
}
```

**HTTP Status Codes:**

- `200 OK` - Success
- `400 Bad Request` - Invalid parameters
- `500 Internal Server Error` - Search engine error

---

#### 2. Browse Products by Category

Retrieve products within a specific category.

**Endpoint:** `GET /search/products/by-category`

**Query Parameters:**

| Parameter    | Type    | Required | Default | Description                |
|--------------|---------|----------|---------|----------------------------|
| `categoryId` | string  | Yes      | -       | Category identifier        |
| `page`       | integer | No       | 0       | Page number (0-indexed)    |
| `size`       | integer | No       | 20      | Results per page (max: 100)|

**Request Example:**

```http
GET /search/products/by-category?categoryId=c10&page=0&size=20
```

**Response:** [ProductSearchResponseDTO](#productsearchresponsedto)

**Response Example:**

```json
{
  "products": [
    {
      "productId": "p201",
      "name": "Samsung Galaxy S24",
      "categoryName": "Smartphones",
      "minPrice": 79900.0,
      "merchantCount": 12,
      "inStock": true,
      "avgRating": 4.5,
      "images": [
        "https://cdn.example.com/galaxy-s24.jpg"
      ]
    }
  ],
  "totalHits": 156,
  "page": 0,
  "size": 20,
  "facets": null
}
```

**HTTP Status Codes:**

- `200 OK` - Success
- `400 Bad Request` - Missing or invalid categoryId
- `404 Not Found` - Category does not exist
- `500 Internal Server Error` - Search engine error

---

#### 3. Search with Filters

Advanced search with multiple filter criteria.

**Endpoint:** `GET /search/products/filter`

**Query Parameters:**

| Parameter            | Type    | Required | Default | Description                           |
|----------------------|---------|----------|---------|---------------------------------------|
| `q`                  | string  | No       | -       | Search query (keywords)               |
| `categoryId`         | string  | No       | -       | Filter by category                    |
| `minPrice`           | double  | No       | -       | Minimum price (inclusive)             |
| `maxPrice`           | double  | No       | -       | Maximum price (inclusive)             |
| `inStockOnly`        | boolean | No       | false   | Show only in-stock products           |
| `minMerchantRating`  | double  | No       | -       | Minimum merchant rating (0.0 - 5.0)   |
| `minProductRating`   | double  | No       | -       | Minimum product rating (0.0 - 5.0)    |
| `page`               | integer | No       | 0       | Page number (0-indexed)               |
| `size`               | integer | No       | 20      | Results per page (max: 100)           |

**Request Example:**

```http
GET /search/products/filter?q=laptop&categoryId=c20&minPrice=50000&maxPrice=150000&inStockOnly=true&minProductRating=4.0&page=0&size=20
```

**Response:** [ProductSearchResponseDTO](#productsearchresponsedto)

**Response Example:**

```json
{
  "products": [
    {
      "productId": "p301",
      "name": "Dell XPS 15",
      "categoryName": "Laptops",
      "minPrice": 129900.0,
      "merchantCount": 4,
      "inStock": true,
      "avgRating": 4.6,
      "images": [
        "https://cdn.example.com/dell-xps-15.jpg"
      ]
    },
    {
      "productId": "p302",
      "name": "MacBook Pro 14",
      "categoryName": "Laptops",
      "minPrice": 149900.0,
      "merchantCount": 6,
      "inStock": true,
      "avgRating": 4.8,
      "images": [
        "https://cdn.example.com/macbook-pro-14.jpg"
      ]
    }
  ],
  "totalHits": 8,
  "page": 0,
  "size": 20,
  "facets": null
}
```

**HTTP Status Codes:**

- `200 OK` - Success
- `400 Bad Request` - Invalid filter values
- `500 Internal Server Error` - Search engine error

---

### Offer Ranking

#### 4. Get Ranked Offers for Product

Retrieve ranked offers for a specific product (used on Product Detail Pages).

**Endpoint:** `GET /search/products/{productId}/offers`

**Path Parameters:**

| Parameter   | Type   | Required | Description           |
|-------------|--------|----------|-----------------------|
| `productId` | string | Yes      | Product identifier    |

**Request Example:**

```http
GET /search/products/p123/offers
```

**Response:** Array of [OfferResponseDTO](#offerresponsedto)

**Response Example:**

```json
[
  {
    "merchantId": "m456",
    "price": 129900.0,
    "currency": "INR",
    "availableQty": 25,
    "merchantRating": 4.8
  },
  {
    "merchantId": "m789",
    "price": 131900.0,
    "currency": "INR",
    "availableQty": 10,
    "merchantRating": 4.7
  },
  {
    "merchantId": "m101",
    "price": 132900.0,
    "currency": "INR",
    "availableQty": 50,
    "merchantRating": 4.9
  }
]
```

**Ranking Algorithm:**

Offers are ranked using a weighted scoring system:

| Factor                | Weight | Description                          |
|-----------------------|--------|--------------------------------------|
| Price                 | 30%    | Lower price = higher score           |
| Merchant Rating       | 25%    | Higher rating = higher score         |
| Product Rating        | 15%    | Higher rating = higher score         |
| Stock Availability    | 10%    | In stock = bonus score               |
| Merchant Sales Volume | 10%    | Higher volume = higher score (log)   |
| Merchant Catalog Size | 10%    | Larger catalog = higher score (log)  |

**HTTP Status Codes:**

- `200 OK` - Success (empty array if no offers)
- `400 Bad Request` - Invalid productId format
- `404 Not Found` - Product does not exist
- `500 Internal Server Error` - Search engine error

---

### Autocomplete/Suggestions

#### 5. Get Autocomplete Suggestions

Provide typeahead suggestions based on user input.

**Endpoint:** `GET /search/suggest`

**Query Parameters:**

| Parameter | Type    | Required | Default | Description                    |
|-----------|---------|----------|---------|--------------------------------|
| `prefix`  | string  | Yes      | -       | User's partial input           |
| `limit`   | integer | No       | 10      | Maximum suggestions (max: 20)  |

**Request Example:**

```http
GET /search/suggest?prefix=iph&limit=5
```

**Response:** Array of strings

**Response Example:**

```json
[
  "iPhone 15 Pro Max",
  "iPhone 15 Pro",
  "iPhone 15",
  "iPhone 14 Pro Max",
  "iPhone 14"
]
```

**HTTP Status Codes:**

- `200 OK` - Success (empty array if no suggestions)
- `400 Bad Request` - Missing or invalid prefix
- `500 Internal Server Error` - Search engine error

---

### Health & Admin

#### 6. Database Health Check

Check connectivity to MongoDB and PostgreSQL databases.

**Endpoint:** `GET /api/health/databases`

**Request Example:**

```http
GET /api/health/databases
```

**Response Example (Healthy):**

```json
{
  "mongodb": {
    "status": "UP",
    "database": "ecommerce_db",
    "productCount": 15420
  },
  "postgresql": {
    "status": "UP",
    "offerCount": 48765,
    "inventoryCount": 48765
  }
}
```

**Response Example (Unhealthy):**

```json
{
  "mongodb": {
    "status": "DOWN",
    "error": "Connection timeout"
  },
  "postgresql": {
    "status": "UP",
    "offerCount": 48765,
    "inventoryCount": 48765
  }
}
```

**HTTP Status Codes:**

- `200 OK` - All databases healthy
- `503 Service Unavailable` - One or more databases down

---

#### 7. Trigger Snapshot Load (Admin)

Manually trigger a full snapshot load from databases to Elasticsearch.

**Endpoint:** `POST /api/indexer/snapshot/load`

**Security:** Should be protected (admin-only access)

**Request Example:**

```http
POST /api/indexer/snapshot/load
```

**Response Example:**

```json
"Snapshot load completed successfully"
```

**HTTP Status Codes:**

- `200 OK` - Snapshot load completed
- `500 Internal Server Error` - Snapshot load failed

**Note:** This is a long-running operation. Consider implementing async processing with job status tracking.

---

## Data Models

### ProductSearchResponseDTO

Response for product search operations.

```json
{
  "products": [ProductSummaryDTO],
  "totalHits": 0,
  "page": 0,
  "size": 20,
  "facets": [FacetDTO] // Currently null, coming soon
}
```

**Fields:**

| Field       | Type                          | Description                        |
|-------------|-------------------------------|------------------------------------|
| `products`  | Array<ProductSummaryDTO>      | List of product results            |
| `totalHits` | long                          | Total number of matching products  |
| `page`      | integer                       | Current page number (0-indexed)    |
| `size`      | integer                       | Results per page                   |
| `facets`    | Array<FacetDTO> (nullable)    | Facet counts for filtering (future)|

---

### ProductSummaryDTO

Summary information for a product in search results.

```json
{
  "productId": "p123",
  "name": "iPhone 15 Pro Max",
  "categoryName": "Smartphones",
  "minPrice": 129900.0,
  "merchantCount": 5,
  "inStock": true,
  "avgRating": 4.7,
  "images": [
    "https://cdn.example.com/iphone15-1.jpg"
  ]
}
```

**Fields:**

| Field           | Type            | Description                              |
|-----------------|-----------------|------------------------------------------|
| `productId`     | string          | Unique product identifier                |
| `name`          | string          | Product name                             |
| `categoryName`  | string          | Category name                            |
| `minPrice`      | double          | Lowest price across all offers           |
| `merchantCount` | integer         | Number of merchants selling this product |
| `inStock`       | boolean         | Whether product is available             |
| `avgRating`     | double          | Average customer rating (0.0 - 5.0)      |
| `images`        | Array<string>   | Product image URLs                       |

---

### OfferResponseDTO

Offer details for product detail page.

```json
{
  "merchantId": "m456",
  "price": 129900.0,
  "currency": "INR",
  "availableQty": 25,
  "merchantRating": 4.8
}
```

**Fields:**

| Field            | Type    | Description                         |
|------------------|---------|-------------------------------------|
| `merchantId`     | string  | Merchant identifier                 |
| `price`          | double  | Offer price                         |
| `currency`       | string  | Currency code (ISO 4217)            |
| `availableQty`   | integer | Available quantity                  |
| `merchantRating` | double  | Merchant rating (0.0 - 5.0)         |

---

### FacetDTO

Facet information for filter navigation (future feature).

```json
{
  "name": "brand",
  "value": "Apple",
  "count": 42
}
```

**Fields:**

| Field   | Type    | Description                           |
|---------|---------|---------------------------------------|
| `name`  | string  | Facet type (brand, price_range, etc.) |
| `value` | string  | Facet value                           |
| `count` | long    | Number of products matching facet     |

---

## Error Handling

### Standard Error Response

All errors return a JSON response with the following structure:

```json
{
  "timestamp": "2026-01-30T14:32:15.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid search query",
  "path": "/search/products"
}
```

**Fields:**

| Field       | Type    | Description                    |
|-------------|---------|--------------------------------|
| `timestamp` | string  | ISO 8601 timestamp             |
| `status`    | integer | HTTP status code               |
| `error`     | string  | Error type                     |
| `message`   | string  | Human-readable error message   |
| `path`      | string  | Request path that caused error |

---

### HTTP Status Codes

| Code | Description                                    |
|------|------------------------------------------------|
| 200  | Success                                        |
| 400  | Bad Request - Invalid parameters               |
| 404  | Not Found - Resource does not exist            |
| 500  | Internal Server Error - Search engine failure  |
| 503  | Service Unavailable - Dependencies down        |

---

### Common Error Scenarios

#### 1. Invalid Page/Size Parameters

**Request:**

```http
GET /search/products?q=laptop&page=-1&size=1000
```

**Response:**

```json
{
  "timestamp": "2026-01-30T14:32:15.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Page number must be >= 0 and size must be <= 100",
  "path": "/search/products"
}
```

---

#### 2. Missing Required Parameter

**Request:**

```http
GET /search/products/by-category?page=0
```

**Response:**

```json
{
  "timestamp": "2026-01-30T14:32:15.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Required parameter 'categoryId' is missing",
  "path": "/search/products/by-category"
}
```

---

#### 3. Elasticsearch Connection Error

**Request:**

```http
GET /search/products?q=laptop
```

**Response:**

```json
{
  "timestamp": "2026-01-30T14:32:15.123Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Failed to execute product search query",
  "path": "/search/products"
}
```

---

## Rate Limiting

**Current:** No rate limiting implemented.

**Recommended:**

- **Authenticated users:** 1000 requests/hour per API key
- **Anonymous users:** 100 requests/hour per IP address
- **Burst allowance:** 20 requests/minute

**Rate Limit Headers (Future):**

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 987
X-RateLimit-Reset: 1706623935
```

---

## Search Query Guidelines

### Best Practices

1. **Use specific keywords** - "iphone 15 pro" vs. "phone"
2. **Combine filters** - Use category + price range for better results
3. **Pagination** - Keep page size reasonable (20-50 items)
4. **Sort wisely** - Default relevance sort often works best

### Query Syntax

- **Multi-word queries** - Automatic phrase matching
- **Partial word matching** - Not supported (use autocomplete)
- **Boolean operators** - Not supported
- **Wildcards** - Not supported

### Field Weights

Full-text search uses the following field weights:

| Field         | Weight | Description                     |
|---------------|--------|---------------------------------|
| `name`        | 3x     | Product name (highest priority) |
| `usp`         | 2x     | Unique selling points           |
| `description` | 1x     | Product description             |

---

## Integration Examples

### JavaScript (Fetch API)

```javascript
// Basic product search
async function searchProducts(query, page = 0) {
  const response = await fetch(
    `http://localhost:8083/search/products?q=${encodeURIComponent(query)}&page=${page}&size=20`
  );
  
  if (!response.ok) {
    throw new Error(`Search failed: ${response.statusText}`);
  }
  
  return await response.json();
}

// Usage
const results = await searchProducts('iphone', 0);
console.log(`Found ${results.totalHits} products`);
results.products.forEach(product => {
  console.log(`${product.name} - ₹${product.minPrice}`);
});
```

---

### Python (Requests)

```python
import requests

# Search with filters
def search_with_filters(query, category_id, min_price, max_price):
    url = "http://localhost:8083/search/products/filter"
    params = {
        "q": query,
        "categoryId": category_id,
        "minPrice": min_price,
        "maxPrice": max_price,
        "inStockOnly": True,
        "page": 0,
        "size": 20
    }
    
    response = requests.get(url, params=params)
    response.raise_for_status()
    
    return response.json()

# Usage
results = search_with_filters("laptop", "c20", 50000, 150000)
print(f"Found {results['totalHits']} laptops")
```

---

### Java (Spring RestTemplate)

```java
// Get ranked offers
public List<OfferResponseDTO> getRankedOffers(String productId) {
    String url = "http://localhost:8083/search/products/{productId}/offers";
    
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<OfferResponseDTO[]> response = restTemplate.getForEntity(
        url, 
        OfferResponseDTO[].class, 
        productId
    );
    
    return Arrays.asList(response.getBody());
}
```

---

### cURL

```bash
# Full-text search
curl -X GET "http://localhost:8083/search/products?q=iphone&page=0&size=20"

# Browse by category
curl -X GET "http://localhost:8083/search/products/by-category?categoryId=c10&page=0&size=20"

# Search with filters
curl -X GET "http://localhost:8083/search/products/filter?q=laptop&minPrice=50000&maxPrice=150000&inStockOnly=true"

# Get ranked offers
curl -X GET "http://localhost:8083/search/products/p123/offers"

# Autocomplete
curl -X GET "http://localhost:8083/search/suggest?prefix=iph&limit=5"

# Health check
curl -X GET "http://localhost:8083/api/health/databases"

# Trigger snapshot load (admin)
curl -X POST "http://localhost:8083/api/indexer/snapshot/load"
```

---

## Performance Considerations

### Response Times (Target)

| Endpoint                  | P50    | P95    | P99    |
|---------------------------|--------|--------|--------|
| Product Search            | < 50ms | < 100ms| < 200ms|
| Category Browse           | < 30ms | < 80ms | < 150ms|
| Filtered Search           | < 80ms | < 150ms| < 300ms|
| Offer Ranking             | < 40ms | < 90ms | < 180ms|
| Autocomplete              | < 20ms | < 50ms | < 100ms|

### Optimization Tips

1. **Use pagination** - Always specify page size
2. **Cache on client** - Cache category/facet metadata
3. **Debounce autocomplete** - Wait 300ms after last keystroke
4. **Prefer filters over search** - When possible, use category browsing
5. **Batch offer requests** - If loading multiple products

---

## Future Enhancements

### Planned Features

- ✅ **Faceted Search** - Dynamic filter generation
- ✅ **Geo-location** - Location-based merchant filtering
- ✅ **Personalization** - User history-based ranking
- ✅ **Spell Correction** - "Did you mean...?" suggestions
- ✅ **Related Products** - "Customers also viewed"
- ✅ **Price History** - Track price changes over time
- ✅ **API Key Authentication** - Secure API access
- ✅ **GraphQL Support** - Alternative query interface
- ✅ **Webhooks** - Real-time index update notifications

---

## Changelog

### Version 1.0 (2026-01-30)

- Initial API release
- Product search endpoints
- Offer ranking
- Autocomplete/suggestions
- Health check endpoints
- Snapshot loading (admin)

---

## Support & Contact

**Issues:** Report bugs and feature requests via GitHub Issues  
**Documentation:** https://docs.example.com/search-api  
**Status Page:** https://status.example.com

---

## Appendix

### Example Use Cases

#### 1. Product Listing Page (PLP)

```javascript
// Search with category and filters
const response = await fetch(
  'http://localhost:8083/search/products/filter?' + 
  'categoryId=c10&' +
  'minPrice=20000&' +
  'maxPrice=100000&' +
  'inStockOnly=true&' +
  'page=0&' +
  'size=24'
);
const data = await response.json();
// Render products in grid layout
```

#### 2. Product Detail Page (PDP)

```javascript
// Get ranked offers for product
const productId = 'p123';
const response = await fetch(
  `http://localhost:8083/search/products/${productId}/offers`
);
const offers = await response.json();
// Display offers sorted by ranking
```

#### 3. Search Bar with Autocomplete

```javascript
// Debounced autocomplete
let timeout;
searchInput.addEventListener('input', (e) => {
  clearTimeout(timeout);
  timeout = setTimeout(async () => {
    const response = await fetch(
      `http://localhost:8083/search/suggest?prefix=${e.target.value}&limit=5`
    );
    const suggestions = await response.json();
    // Display suggestions dropdown
  }, 300);
});
```

---

## Glossary

| Term              | Definition                                           |
|-------------------|------------------------------------------------------|
| **CDC**           | Change Data Capture - real-time database sync        |
| **Elasticsearch** | Search engine powering the service                   |
| **Facet**         | Filter category with counts (e.g., brand, price)     |
| **PDP**           | Product Detail Page                                  |
| **PLP**           | Product Listing Page                                 |
| **USP**           | Unique Selling Points - key product features         |
| **Offer**         | A merchant's listing for a product at a specific price|
| **Merchant**      | Seller offering products on the platform             |

---

**End of API Contract**