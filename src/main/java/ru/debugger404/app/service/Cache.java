package ru.debugger404.app.service;

import java.util.Optional;

/**
 * Интерфейс кэша.
 * @param <K>
 * @param <V>
 */

public interface Cache<K, V> {

    boolean put(K key, V value);

    Optional<V> get(K key);

    int size();

    boolean isEmpty();

    void clear();
}
