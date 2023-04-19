package com.tto.gpt.common;


import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

public class ExpiringMap<K, V> extends WeakHashMap<K, V> {
    private final Map<Object, Long> expirations;
    private final int expiredSeconds;
    public ExpiringMap(int expiredSeconds) {
        this.expiredSeconds = expiredSeconds;
        expirations = new WeakHashMap<>();
        new Thread(() -> {
            while (true) {
                expirations.entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue());
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    @Override
    public V put(K key, V value) {
        expirations.put(key, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiredSeconds));
        return super.put(key, value);
    }
}