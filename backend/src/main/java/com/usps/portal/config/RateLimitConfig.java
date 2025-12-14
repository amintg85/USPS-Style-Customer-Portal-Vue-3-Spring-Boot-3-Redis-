package com.usps.portal.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    @Bean
    public Map<String, Bucket> cache() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public RateLimitService rateLimitService() {
        return new RateLimitService();
    }

    @Component
    public static class RateLimitService {
        private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

        public Bucket resolveBucket(String key) {
            return cache.computeIfAbsent(key, k -> {
                Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
                return Bucket.builder()
                    .addLimit(limit)
                    .build();
            });
        }
    }
}


