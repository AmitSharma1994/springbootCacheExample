package com.redis.config;

import java.util.concurrent.Callable;
import java.util.LinkedHashMap;

import org.springframework.cache.Cache;

public class MaxKeysCache implements Cache {

    private final Cache delegate;
    private final int maxKeys;

    private final LinkedHashMap<Object, Boolean> lruTracker;

    public MaxKeysCache(Cache delegate, int maxKeys) {
        this.delegate = delegate;
        this.maxKeys = maxKeys;
        this.lruTracker = new LinkedHashMap<>(16, 0.75f, true);
    }

    // returns cache name which is "products" in our case from @Cacheable(value =
    // "products")
    @Override
    public String getName() {
        return delegate.getName();
    }

    // return which cache is used (redis in our case)
    // particularly the connection
    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    // check if value is cached in Redis,
    // if found touch LRU tracker, if not found return null
    @Override
    public synchronized ValueWrapper get(Object key) {
        ValueWrapper result = delegate.get(key);
        if (result != null) {
            lruTracker.get(key);
        }
        return result;
    }

    // typed version of get(),
    // same logic but returns T instead of ValueWrapper

    @Override
    public synchronized <T> T get(Object key, Class<T> type) {
        T result = delegate.get(key, type);
        if (result != null) {
            lruTracker.get(key);
        }
        return result;
    }

    // store value in Redis,
    // track key in LRU, evict oldest if over 5 via evictIfOverLimit()

    @Override
    public synchronized void put(Object key, Object value) {
        lruTracker.put(key, Boolean.TRUE);
        evictIfOverLimit();
        delegate.put(key, value);
    }

    // get or compute if missing,
    // always tracks since key will exist after this

    @Override
    public synchronized <T> T get(Object key, Callable<T> valueLoader) {
        T result = delegate.get(key, valueLoader);
        lruTracker.put(key, Boolean.TRUE);
        evictIfOverLimit();
        return result;
    }

    // only store if key is new,
    // track + enforce limit only when actually added

    @Override
    public synchronized ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper existing = delegate.putIfAbsent(key, value);
        if (existing == null) {
            lruTracker.put(key, Boolean.TRUE);
            evictIfOverLimit();
        }
        return existing;
    }

    // remove single key from both Redis and LRU tracker
    @Override
    public synchronized void evict(Object key) {
        delegate.evict(key);
        lruTracker.remove(key);
    }

    // wipe all keys from Redis and LRU tracker
    @Override
    public synchronized void clear() {
        delegate.clear();
        lruTracker.clear();
    }

    private void evictIfOverLimit() {
        while (lruTracker.size() > maxKeys) {
            Object eldestKey = lruTracker.keySet().iterator().next();
            delegate.evict(eldestKey);
            lruTracker.remove(eldestKey);
            System.out.println("[MaxKeysCache] LRU evicted key: " + eldestKey
                    + " from cache: " + getName());
        }
    }

}
