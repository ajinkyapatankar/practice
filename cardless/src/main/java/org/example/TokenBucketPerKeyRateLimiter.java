package org.example;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public final class TokenBucketPerKeyRateLimiter {
    private final int capacity;
    private final double refillRatePerSecond;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketPerKeyRateLimiter(int capacity, double refillRatePerSecond) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        if (refillRatePerSecond <= 0) {
            throw new IllegalArgumentException("refillRatePerSecond must be positive");
        }
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    public boolean tryConsume(String key) {
        Objects.requireNonNull(key, "key");
        if (key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }

        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(capacity, refillRatePerSecond));
        return bucket.tryConsume();
    }

    private static final class Bucket {
        private final ReentrantLock lock = new ReentrantLock();
        private final int capacity;
        private final double refillRatePerSecond;
        private double availableTokens;
        private long lastRefillNanos;

        private Bucket(int capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
            this.availableTokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        private boolean tryConsume() {
            lock.lock();
            try {
                refillLocked();
                if (availableTokens < 1.0) {
                    return false;
                }
                availableTokens -= 1.0;
                return true;
            } finally {
                lock.unlock();
            }
        }

        private void refillLocked() {
            long now = System.nanoTime();
            long elapsedNanos = now - lastRefillNanos;
            if (elapsedNanos <= 0) {
                return;
            }

            double tokensToAdd = elapsedNanos * (refillRatePerSecond / 1_000_000_000.0);
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillNanos = now;
        }
    }
}
