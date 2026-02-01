package com.example.ecomm1.product.service;

import com.example.ecomm1.common.config.SecurityUtils;
import com.example.ecomm1.product.dto.CreateProductRequest;
import com.example.ecomm1.product.dto.ProductResponse;
import com.example.ecomm1.product.exception.ProductNotFoundException;
import com.example.ecomm1.product.model.Product;
import com.example.ecomm1.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(CreateProductRequest request) {
        SecurityUtils.requireRole("MERCHANT");
        log.info("Creating product name={} category={}", request.getName(), request.getCategory());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .brand(request.getBrand())
                .imageUrl(request.getImageUrl())
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    public ProductResponse getProductById(String id) {
        log.debug("Fetching product by id={}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return toResponse(product);
    }

    public List<ProductResponse> searchProducts(String name, String category) {
        log.debug("Searching products name={} category={}", name, category);
        boolean hasName = name != null && !name.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (!hasName && !hasCategory) {
            throw new IllegalArgumentException("At least one search parameter (name or category) is required");
        }

        List<Product> results;
        if (hasName && hasCategory) {
            results = productRepository.findByCategoryAndNameContainingIgnoreCase(category, name);
        } else if (hasName) {
            results = productRepository.findByNameContainingIgnoreCase(name);
        } else {
            results = productRepository.findByCategory(category);
        }

        return results.stream().map(this::toResponse).toList();
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .brand(product.getBrand())
                .imageUrl(product.getImageUrl())
                .build();
    }
}

