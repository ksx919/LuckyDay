package com.luckyDay.common.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalCache {
    private static Map<String,Object> cache = new ConcurrentHashMap();

    public static void put(String key,Object val){
        cache.put(key,val);
    }

    public static Boolean containsKey(String key){
        if (key == null) return false;
        return cache.containsKey(key);
    }

    public static void rem(String key) {
        cache.remove(key);
    }
}
