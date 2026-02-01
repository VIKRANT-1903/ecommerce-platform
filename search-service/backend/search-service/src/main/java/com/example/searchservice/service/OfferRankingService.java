package com.example.searchservice.service;

import com.example.searchservice.controller.dto.response.OfferResponseDTO;

import java.util.List;

public interface OfferRankingService {

    List<OfferResponseDTO> getRankedOffersForProduct(String productId);
}
