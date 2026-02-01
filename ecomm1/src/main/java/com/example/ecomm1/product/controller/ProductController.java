package com.example.ecomm1.product.controller;

import com.example.ecomm1.common.dto.ApiResponse;
import com.example.ecomm1.product.dto.CreateProductRequest;
import com.example.ecomm1.product.dto.ProductResponse;
import com.example.ecomm1.product.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        log.info("Creating product name={} category={}", request.getName(), request.getCategory());
        ProductResponse created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Product created", "/products"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable @NotBlank String id) {
        log.debug("Fetching product by id={}", id);
        return ResponseEntity.ok(ApiResponse.ok(productService.getProductById(id), "Product fetched", "/products/" + id));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category
    ) {
        log.debug("Searching products name={} category={}", name, category);
        return ResponseEntity.ok(ApiResponse.ok(productService.searchProducts(name, category), "Products fetched", "/products/search"));
    }
}

