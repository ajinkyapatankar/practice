package org.example;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    void tokenBucketAllowsLimitedRequestsPerClientKey() {
        TokenBucketPerKeyRateLimiter limiter = new TokenBucketPerKeyRateLimiter(2, 10.0);

        assertTrue(limiter.tryConsume("client-a"));
        assertTrue(limiter.tryConsume("client-a"));
        assertFalse(limiter.tryConsume("client-a"));

        assertTrue(limiter.tryConsume("client-b"));
        assertTrue(limiter.tryConsume("client-b"));
        assertFalse(limiter.tryConsume("client-b"));
    }

    @Test
    void slidingWindowEnforcesLimitAcrossRequestsForSameKey() throws InterruptedException {
        SlidingWindowPerKeyRateLimiter limiter = new SlidingWindowPerKeyRateLimiter(3, Duration.ofMillis(200));

        assertTrue(limiter.tryConsume("api-key"));
        assertTrue(limiter.tryConsume("api-key"));
        assertTrue(limiter.tryConsume("api-key"));
        assertFalse(limiter.tryConsume("api-key"));

        Thread.sleep(250);
        assertTrue(limiter.tryConsume("api-key"));
    }
}
