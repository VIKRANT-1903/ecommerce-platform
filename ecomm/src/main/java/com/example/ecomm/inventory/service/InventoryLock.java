package com.example.ecomm.inventory.service;

import com.example.ecomm.common.exception.LockAcquisitionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryLock {

    private static final String LOCK_KEY_PREFIX = "lock:inventory:";
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(5);

    private static final String UNLOCK_SCRIPT = """
            if redis.call("get", KEYS[1]) == ARGV[1] then
                return redis.call("del", KEYS[1])
            else
                return 0
            end
            """;

    private final StringRedisTemplate redisTemplate;

    /**
     * Acquires a lock for the given product and merchant. Lock key format: lock:inventory:{productId}:{merchantId}.
     *
     * @return lock token to pass to {@link #unlock} if acquired
     * @throws LockAcquisitionException if lock could not be acquired
     */
    public String lock(String productId, Integer merchantId) {
        String key = lockKey(productId, merchantId);
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, LOCK_TIMEOUT);
        if (Boolean.TRUE.equals(acquired)) {
            return token;
        }
        throw new LockAcquisitionException("Could not acquire inventory lock for product " + productId + ", merchant " + merchantId);
    }

    /**
     * Releases the lock using the token returned from {@link #lock}. No-op if key already expired or token does not match.
     */
    public void unlock(String productId, Integer merchantId, String lockToken) {
        String key = lockKey(productId, merchantId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, List.of(key), lockToken);
        if (result != null && result == 1) {
            log.debug("Released inventory lock for key {}", key);
        }
    }

    private static String lockKey(String productId, Integer merchantId) {
        return LOCK_KEY_PREFIX + productId + ":" + merchantId;
    }
}
