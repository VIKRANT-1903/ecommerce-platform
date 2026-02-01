package com.example.ecomm1.offer.repository;

import com.example.ecomm1.offer.model.Offer;
import com.example.ecomm1.offer.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByProductId(String productId);
    List<Offer> findByMerchantMerchantId(Long merchantId);
    List<Offer> findByStatus(OfferStatus status);
    List<Offer> findByProductIdAndStatus(String productId, OfferStatus status);
    Optional<Offer> findByOfferIdAndMerchant_User_Id(Long offerId, Long userId);
}
