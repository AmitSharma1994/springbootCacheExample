package com.redis.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {

    @Value("${redis.shards[0].host}")
    private String shard0Host;

    @Value("${redis.shards[0].port}")
    private Integer shard0Port;

    @Value("${redis.shards[1].host}")
    private String shard1Host;

    @Value("${redis.shards[1].port}")
    private Integer shard1Port;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            @Qualifier("shard0ConnectionFactory") LettuceConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean("shard0ConnectionFactory")
    @Primary
    public LettuceConnectionFactory shard0ConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(shard0Host, shard0Port);
        return new LettuceConnectionFactory(config);
    }

    @Bean("shard1ConnectionFactory")
    public LettuceConnectionFactory shard1ConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(shard1Host, shard1Port);
        return new LettuceConnectionFactory(config);
    }

    @Bean("shard0CacheManager")
    @Primary
    public RedisCacheManager shard0CacheManager(@Qualifier("shard0ConnectionFactory") LettuceConnectionFactory cf) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(SerializationPair
                        .fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaults)
                .build();
    }

    @Bean("shard1CacheManager")
    public RedisCacheManager shard1CacheManager(@Qualifier("shard1ConnectionFactory") LettuceConnectionFactory cf) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(SerializationPair
                        .fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaults)
                .build();
    }

}
