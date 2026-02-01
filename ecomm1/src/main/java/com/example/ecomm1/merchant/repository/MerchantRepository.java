package com.example.ecomm1.merchant.repository;

import com.example.ecomm1.merchant.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByUser_Id(Long userId);
    boolean existsByUser_Id(Long userId);
}
