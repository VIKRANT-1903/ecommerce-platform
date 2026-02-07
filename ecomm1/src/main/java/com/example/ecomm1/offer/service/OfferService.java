package com.example.ecomm1.offer.service;

import com.example.ecomm1.common.config.SecurityUtils;
import com.example.ecomm1.merchant.model.Merchant;
import com.example.ecomm1.merchant.repository.MerchantRepository;
import com.example.ecomm1.offer.client.InventoryHttpClient;
import com.example.ecomm1.offer.dto.CreateOfferRequest;
import com.example.ecomm1.offer.dto.InventoryOfferRequest;
import com.example.ecomm1.offer.dto.OfferResponse;
import com.example.ecomm1.offer.enums.OfferStatus;
import com.example.ecomm1.offer.exception.OfferNotFoundException;
import com.example.ecomm1.offer.model.Offer;
import com.example.ecomm1.offer.repository.OfferRepository;
import com.example.ecomm1.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final MerchantRepository merchantRepository;
    private final ProductService productService;
    private final InventoryHttpClient inventoryHttpClient;

    @Transactional
    public OfferResponse createOffer(CreateOfferRequest request) {
        SecurityUtils.requireRole("MERCHANT");
        log.info("Creating offer productId={} price={} currency={}", request.getProductId(), request.getPrice(), request.getCurrency());

        // Validate product exists (throws ProductNotFoundException if not)
        productService.getProductById(request.getProductId());

        Long userId = SecurityUtils.getCurrentUserId();
        Merchant merchant = merchantRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AccessDeniedException("Merchant profile not found"));

        Offer offer = Offer.builder()
                .productId(request.getProductId())
                .merchant(merchant)
                .price(request.getPrice())
                .currency(request.getCurrency())
                .status(OfferStatus.ACTIVE)
                .build();

        Offer saved = offerRepository.save(offer);

        // Call Inventory Service (if this fails, transaction rolls back)
        log.info("Calling inventory createOffer offerId={}", saved.getOfferId());
        inventoryHttpClient.createOffer(InventoryOfferRequest.builder()
                .offerId(saved.getOfferId())
                .productId(saved.getProductId())
                .merchantId(saved.getMerchant().getMerchantId())
                .price(saved.getPrice())
                .currency(saved.getCurrency())
                .build());

        return toResponse(saved);
    }

    @Transactional
    public void deleteOffer(Long offerId) {
        SecurityUtils.requireRole("MERCHANT");
        log.info("Deleting offer offerId={}", offerId);

        Long userId = SecurityUtils.getCurrentUserId();
        Offer offer = offerRepository.findByOfferIdAndMerchant_User_Id(offerId, userId)
                .orElseThrow(() -> new OfferNotFoundException("Offer not found"));

        // Call Inventory Service (if this fails, transaction rolls back and offer remains)
        log.info("Calling inventory deleteOffer offerId={}", offerId);
        inventoryHttpClient.deleteOffer(offerId);

        offerRepository.delete(offer);
    }

    public List<OfferResponse> listMyOffers() {
        SecurityUtils.requireRole("MERCHANT");
        Long userId = SecurityUtils.getCurrentUserId();
        log.debug("Listing offers for current merchant userId={}", userId);

        Merchant merchant = merchantRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AccessDeniedException("Merchant profile not found"));

        return offerRepository.findByMerchantMerchantId(merchant.getMerchantId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<OfferResponse> listOffersByProduct(String productId) {
        // Anyone authenticated can view offers by product (protected by SecurityConfig)
        log.debug("Listing offers for productId={}", productId);
        return offerRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // --- NEW BULK METHOD ---
    public Map<String, List<OfferResponse>> getOffersForProducts(List<String> productIds) {
        // 1. Fetch all active offers for these products in one query
        // Note: Ensure your OfferRepository has findByProductIdInAndStatus defined
        List<Offer> allOffers = offerRepository.findByProductIdInAndStatus(productIds, OfferStatus.ACTIVE);

        // 2. Convert entities to DTOs
        List<OfferResponse> responseList = allOffers.stream()
                .map(this::toResponse)
                .toList();

        // 3. Group by Product ID
        return responseList.stream()
                .collect(Collectors.groupingBy(OfferResponse::getProductId));
    }

    private OfferResponse toResponse(Offer offer) {
        return OfferResponse.builder()
                .offerId(offer.getOfferId())
                .productId(offer.getProductId())
                .merchantId(offer.getMerchant() != null ? offer.getMerchant().getMerchantId() : null)
                .price(offer.getPrice())
                .currency(offer.getCurrency())
                .status(offer.getStatus())
                .createdAt(offer.getCreatedAt())
                .updatedAt(offer.getUpdatedAt())
                .build();
    }
}