package org.example;

import java.time.Duration;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TokenBucketPerKeyRateLimiter tokenBucket = new TokenBucketPerKeyRateLimiter(2, 1.0);
        System.out.println("token user-1 attempt 1: " + tokenBucket.tryConsume("user-1"));
        System.out.println("token user-1 attempt 2: " + tokenBucket.tryConsume("user-1"));
        System.out.println("token user-1 attempt 3: " + tokenBucket.tryConsume("user-1"));

        SlidingWindowPerKeyRateLimiter slidingWindow = new SlidingWindowPerKeyRateLimiter(3, Duration.ofSeconds(1));
        System.out.println("window user-2 attempt 1: " + slidingWindow.tryConsume("user-2"));
        System.out.println("window user-2 attempt 2: " + slidingWindow.tryConsume("user-2"));
        System.out.println("window user-2 attempt 3: " + slidingWindow.tryConsume("user-2"));
        System.out.println("window user-2 attempt 4: " + slidingWindow.tryConsume("user-2"));

        Thread.sleep(1100);
        System.out.println("window user-2 after sleep: " + slidingWindow.tryConsume("user-2"));
    }
}
