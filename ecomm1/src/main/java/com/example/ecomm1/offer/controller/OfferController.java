package com.example.ecomm1.offer.controller;

import com.example.ecomm1.common.dto.ApiResponse;
import com.example.ecomm1.offer.dto.CreateOfferRequest;
import com.example.ecomm1.offer.dto.OfferResponse;
import com.example.ecomm1.offer.service.OfferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<ApiResponse<OfferResponse>> create(@Valid @RequestBody CreateOfferRequest request) {
        log.info("Creating offer for productId={}", request.getProductId());
        OfferResponse created = offerService.createOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(created, "Offer created", "/offers"));
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long offerId) {
        log.info("Deleting offer offerId={}", offerId);
        offerService.deleteOffer(offerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OfferResponse>>> myOffers() {
        log.debug("Listing offers for current merchant");
        return ResponseEntity.ok(ApiResponse.ok(offerService.listMyOffers(), "Offers fetched", "/offers/my"));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<OfferResponse>>> offersByProduct(@PathVariable @NotBlank String productId) {
        log.debug("Listing offers for productId={}", productId);
        return ResponseEntity.ok(ApiResponse.ok(offerService.listOffersByProduct(productId), "Offers fetched", "/offers/product/" + productId));
    }
}

