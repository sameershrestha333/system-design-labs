package com.sameer.sdl.system_design_labs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisCacheConfig {

    @Bean
    RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Value("${spring.cache.redis.time-to-live}") Duration ttl
    ) {
        // Lock while loading: one request fetches the missing product from PostgreSQL while the others wait.
        var writer = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);

        // Keep it for 30 seconds: the cached product expires after the TTL set in application.properties.
        var defaults = RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl);

        return RedisCacheManager.builder(writer)
                .cacheDefaults(defaults)
                .build();
    }
}
