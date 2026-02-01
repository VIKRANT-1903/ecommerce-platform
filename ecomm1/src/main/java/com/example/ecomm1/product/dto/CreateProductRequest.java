package com.example.ecomm1.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 255, message = "Category must not exceed 255 characters")
    private String category;

    @Size(max = 255, message = "Brand must not exceed 255 characters")
    private String brand;

    @Size(max = 2000, message = "Image URL must not exceed 2000 characters")
    private String imageUrl;
}

