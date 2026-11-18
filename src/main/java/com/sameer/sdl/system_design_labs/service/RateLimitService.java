package com.sameer.sdl.system_design_labs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimitService {
    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>("""
                    local requestCount = redis.call('INCR', KEYS[1])
                    if requestCount == 1 then
                        redis.call('EXPIRE', KEYS[1], 60)
                    end
                    if requestCount > 5 then
                        return math.max(1, redis.call('TTL', KEYS[1]))
                    end
                    return 0
                    """, Long.class);

    private final StringRedisTemplate redis;

    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public long checkLimit(String clientId) {
        var counterKey = "rate-limit:products:get:" + clientId;
        var retryAfterSeconds = redis.execute(RATE_LIMIT_SCRIPT, List.of(counterKey));
        if (retryAfterSeconds == null) {
            throw new IllegalStateException("Redis returned no rate-limit decision");
        }
        log.info("checkLimit: Redis rate-limit decision allowed={}, retryAfterSeconds={}.",
                retryAfterSeconds == 0, retryAfterSeconds);
        return retryAfterSeconds;
    }
}
