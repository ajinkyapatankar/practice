package org.example;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class SlidingWindowPerKeyRateLimiter {
    private final int maxRequests;
    private final long windowSizeMillis;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public SlidingWindowPerKeyRateLimiter(int maxRequests, Duration windowSize) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be positive");
        }
        Objects.requireNonNull(windowSize, "windowSize");
        if (windowSize.isZero() || windowSize.isNegative()) {
            throw new IllegalArgumentException("windowSize must be positive");
        }

        this.maxRequests = maxRequests;
        this.windowSizeMillis = windowSize.toMillis();
    }

    public boolean tryConsume(String key) {
        Objects.requireNonNull(key, "key");
        if (key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }

        Window window = windows.computeIfAbsent(key, ignored -> new Window(maxRequests, windowSizeMillis));
        return window.tryConsume(System.currentTimeMillis());
    }

    private static final class Window {
        private final int maxRequests;
        private final long windowSizeMillis;
        private final Deque<Long> requestTimes = new ArrayDeque<>();
        private final AtomicLong lastSeenAt = new AtomicLong(System.currentTimeMillis());

        private Window(int maxRequests, long windowSizeMillis) {
            this.maxRequests = maxRequests;
            this.windowSizeMillis = windowSizeMillis;
        }

        private synchronized boolean tryConsume(long now) {
            lastSeenAt.set(now);

            while (!requestTimes.isEmpty() && now - requestTimes.peekFirst() >= windowSizeMillis) {
                requestTimes.pollFirst();
            }

            if (requestTimes.size() >= maxRequests) {
                return false;
            }

            requestTimes.addLast(now);
            return true;
        }
    }
}
