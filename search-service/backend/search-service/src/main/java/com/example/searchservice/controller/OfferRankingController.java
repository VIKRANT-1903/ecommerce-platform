package com.example.searchservice.controller;

import com.example.searchservice.controller.dto.response.OfferResponseDTO;
import com.example.searchservice.service.OfferRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search/products")
@RequiredArgsConstructor
public class OfferRankingController {

    private final OfferRankingService offerRankingService;

    /**
     * Ranked offers for product detail page
     * GET /search/products/{productId}/offers
     */
    @GetMapping("/{productId}/offers")
    public List<OfferResponseDTO> getRankedOffers(
            @PathVariable String productId
    ) {
        return offerRankingService.getRankedOffersForProduct(productId);
    }
}
