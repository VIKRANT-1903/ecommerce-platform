package com.example.searchservice.service.impl;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.searchservice.controller.dto.response.OfferResponseDTO;
import com.example.searchservice.model.document.OfferDocument;
import com.example.searchservice.query.OfferRankingQueryBuilder;
import com.example.searchservice.repository.OfferSearchRepository;
import com.example.searchservice.service.OfferRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferRankingServiceImpl implements OfferRankingService {

    private final OfferRankingQueryBuilder queryBuilder;
    private final OfferSearchRepository repository;

    @Override
    public List<OfferResponseDTO> getRankedOffersForProduct(String productId) {

        var query = queryBuilder.buildOfferRankingQuery(productId);
        SearchResponse<OfferDocument> response = repository.search(query);

        return response.hits()
                .hits()
                .stream()
                .map(hit -> mapToResponse(hit.source()))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    private OfferResponseDTO mapToResponse(OfferDocument doc) {

        if (doc == null) {
            return null;
        }

        return OfferResponseDTO.builder()
                .merchantId(doc.getMerchantId())
                .price(doc.getPrice())
                .currency(doc.getCurrency())
                .availableQty(doc.getAvailableQty())
                .merchantRating(doc.getMerchantRating())
                .build();
    }
}
