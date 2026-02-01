package com.example.ecomm.inventory.service;

import com.example.ecomm.inventory.dto.InventoryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCache {

    private static final String CACHE_KEY_PREFIX = "inventory:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<InventoryResponse> get(String productId, Integer merchantId) {
        String key = cacheKey(productId, merchantId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, InventoryResponse.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached inventory for key {}", key, e);
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public void put(InventoryResponse response) {
        String key = cacheKey(response.productId(), response.merchantId());
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(key, json, TTL);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize inventory for cache, key {}", key, e);
        }
    }

    public void evict(String productId, Integer merchantId) {
        redisTemplate.delete(cacheKey(productId, merchantId));
    }

    private static String cacheKey(String productId, Integer merchantId) {
        return CACHE_KEY_PREFIX + productId + ":" + merchantId;
    }
}
