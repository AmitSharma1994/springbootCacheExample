package com.redis.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;

import com.redis.entity.Product;

@Component("shardCacheResolver")
public class ShardCacheResolver implements CacheResolver {

    private final CacheManager shard0CacheManager;
    private final CacheManager shard1CacheManager;

    private final Map<String, MaxKeysCache> wrappedCaches = new ConcurrentHashMap<>();

    public ShardCacheResolver(
            @Qualifier("shard0CacheManager") CacheManager shard0CacheManager,
            @Qualifier("shard1CacheManager") CacheManager shard1CacheManager) {
        this.shard0CacheManager = shard0CacheManager;
        this.shard1CacheManager = shard1CacheManager;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Object[] args = context.getArgs();
        long id;

        // 1. Extract the product ID from method arguments
        // Spring gives us access to the arguments of the annotated method
        // e.g., getProductDetailsbyid(long id) → args[0] is the id
        // e.g., updateProductDetails(Product product) → args[0] is the Product
        if (args[0] instanceof Product product) {
            id = product.getId();
        } else {
            id = (long) args[0];
        }

        // 2. Shard routing: id % 2 decides which Redis node handles this key
        // Even IDs (2,4,6) → shard 0 (Redis :6379)
        // Odd IDs (1,3,5) → shard 1 (Redis :6380)

        int shardIndex = (int) (Math.abs(id) % 2);
        CacheManager cacheManager = (shardIndex == 0 ? shard0CacheManager : shard1CacheManager);

        String cacheName = "products-shard-" + shardIndex;
        Cache rawCache = cacheManager.getCache(cacheName);

        // 4. Wrap with MaxKeysCache to enforce the 5-key limit
        // computeIfAbsent → first call creates the wrapper, subsequent calls reuse it
        // This is why wrappedCaches is a ConcurrentHashMap — thread-safe lazy init
        MaxKeysCache wrappedCache = wrappedCaches.computeIfAbsent(cacheName, name -> new MaxKeysCache(rawCache, 5));

        return Collections.singleton(wrappedCache);

    }

}
