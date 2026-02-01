package com.example.ecomm1.common.exception;

import com.example.ecomm1.auth.exception.AuthenticationException;
import com.example.ecomm1.common.dto.ApiError;
import com.example.ecomm1.common.dto.ApiResponse;
import com.example.ecomm1.merchant.exception.MerchantNotFoundException;
import com.example.ecomm1.offer.exception.OfferNotFoundException;
import com.example.ecomm1.product.exception.ProductNotFoundException;
import com.example.ecomm1.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication error on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = ApiError.builder()
                .code("AUTHENTICATION_FAILED")
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(error, "Unauthorized", request.getRequestURI()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .code("USER_NOT_FOUND")
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, "Not Found", request.getRequestURI()));
    }

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMerchantNotFoundException(MerchantNotFoundException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .code("MERCHANT_NOT_FOUND")
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, "Not Found", request.getRequestURI()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFoundException(ProductNotFoundException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .code("PRODUCT_NOT_FOUND")
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, "Not Found", request.getRequestURI()));
    }

    @ExceptionHandler(OfferNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOfferNotFoundException(OfferNotFoundException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .code("OFFER_NOT_FOUND")
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(error, "Not Found", request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .code("BAD_REQUEST")
                .detail(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Bad Request", request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiError apiError = ApiError.builder()
                .code("VALIDATION_ERROR")
                .detail("Request validation failed")
                .fieldErrors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(apiError, "Bad Request", request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError error = ApiError.builder()
                .code("FORBIDDEN")
                .detail("Forbidden")
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(error, "Forbidden", request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .code("TYPE_MISMATCH")
                .detail("Invalid value for parameter '" + ex.getName() + "'")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Bad Request", request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .code("MALFORMED_JSON")
                .detail("Malformed JSON request")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(error, "Bad Request", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error on {}", request.getRequestURI(), ex);
        ApiError error = ApiError.builder()
                .code("INTERNAL_ERROR")
                .detail("Unexpected error")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(error, "Internal Server Error", request.getRequestURI()));
    }
}
