package ru.debugger404.app.service;

import ru.debugger404.app.util.CacheElement;
import ru.debugger404.app.util.DoublyLinkedList;
import ru.debugger404.app.util.LinkedListNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Реализация LRU кэша.
 * @param <K>
 * @param <V>
 */

public class LruCache<K, V> implements Cache<K, V> {

    private int size;
    private Map<K, LinkedListNode<CacheElement<K, V>>> linkedListNodeMap;
    private DoublyLinkedList<CacheElement<K, V>> doublyLinkedList;
    /*
    ReentrantReadWriteLock — это реалиализация интерфейса ReadWriteLock, которая предоставляет пару блокировок для
    чтения и записи. Используется, когда в системе много операций чтения и мало операций записи. Позволяет нескольким
    потокам читать данные параллельно, при этом только один поток может писать данные за раз. Это улучшает производительность
     за счёт уменьшения конкуренции между потоками.
     */
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LruCache(int size) {
        this.size = size;
        this.linkedListNodeMap = new HashMap<>();
        this.doublyLinkedList = new DoublyLinkedList<>();
    }

    @Override
    public boolean put(K key, V value) {
        CacheElement<K, V> item = new CacheElement<K, V>(key, value);
        LinkedListNode<CacheElement<K, V>> newNode;
        if (this.linkedListNodeMap.containsKey(key)) {
            LinkedListNode<CacheElement<K, V>> node = this.linkedListNodeMap.get(key);
            newNode = doublyLinkedList.updateAndMoveToFront(node, item);
        } else {
            if (this.size() >= this.size) {
                this.evictElement();
            }
            newNode = this.doublyLinkedList.add(item);
        }
        if (newNode.isEmpty()) {
            return false;
        }
        this.linkedListNodeMap.put(key, newNode);
        return true;
    }

    @Override
    public Optional<V> get(K key) {
        this.lock.readLock().lock(); // Блокировка чтения.
        try {
            LinkedListNode<CacheElement<K, V>> linkedListNode = this.linkedListNodeMap.get(key);
            if (linkedListNode != null && !linkedListNode.isEmpty()) {
                linkedListNodeMap.put(key, this.doublyLinkedList.moveToFront(linkedListNode));
                return Optional.of(linkedListNode.getElement().getValue());
            }
            return Optional.empty();
        } finally {
            this.lock.readLock().lock();
        }
    }

    @Override
    public int size() {
        this.lock.readLock().lock();
        try {
            return doublyLinkedList.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void clear() {
        this.lock.writeLock().lock();
        try {
            linkedListNodeMap.clear();
            doublyLinkedList.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /*
     Безопасная (с точки зрения многопоточности) операцию удаления последнего элемента из двусвязного списка и очистку
     соответствующей мапы.
     */
    private boolean evictElement() {
        this.lock.writeLock().lock(); // Блокировка записи.

        try {
            LinkedListNode<CacheElement<K, V>> linkedListNode = doublyLinkedList.removeTail();
            if (linkedListNode.isEmpty()) {
                return false;
            }
            linkedListNodeMap.remove(linkedListNode.getElement().getKey());
            return true;
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
