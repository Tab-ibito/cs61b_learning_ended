package hashmap;

import java.util.*;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private final int initialSize = 16;
    private double loadFactor = 0.75;
    private int size = 0;
    private int items = 0;

    /**
     * Constructors
     */
    public MyHashMap() {
        this.size = this.initialSize;
        buckets = createTable(initialSize);
    }

    public MyHashMap(int initialSize) {
        this.size = initialSize;
        buckets = createTable(initialSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.size = initialSize;
        buckets = createTable(initialSize);
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    private Collection<Node>[] resize() {
        int newSize = size * 2;
        Collection<Node>[] a = createTable(newSize);
        for (int i = 0; i < size; i++) {
            Iterator<Node> iter = buckets[i].iterator();
            while (iter.hasNext()) {
                Node cur = iter.next();
                int index = cur.key.hashCode() & (size - 1);
                a[index].add(cur);
            }
        }
        size *= 2;
        return a;
    }

    @Override
    public void clear() {
        size = initialSize;
        items = 0;
        buckets = createTable(size);
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @Override
    public int size() {
        return items;
    }

    @Override
    public V get(K key) {
        for (int i = 0; i < size; i++) {
            Iterator<Node> iter = buckets[i].iterator();
            while (iter.hasNext()) {
                Node cur = iter.next();
                if (cur.key.equals(key)) {
                    return cur.value;
                }
            }
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        int index = key.hashCode() & (size - 1);
        if (get(key) != null) {
            Iterator<Node> iter = buckets[index].iterator();
            while (iter.hasNext()) {
                Node cur = iter.next();
                if (cur.key.equals(key)) {
                    cur.value = value;
                }
            }
            return;
        }
        items += 1;
        buckets[index].add(createNode(key, value));
        if ((double) items / size >= loadFactor) {
            buckets = resize();
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            Iterator<Node> iter = buckets[i].iterator();
            while (iter.hasNext()) {
                Node cur = iter.next();
                set.add(cur.key);
            }
        }
        return set;
    }

    @Override
    public V remove(K key) {
        for (int i = 0; i < size; i++) {
            Iterator<Node> iter = buckets[i].iterator();
            while (iter.hasNext()) {
                Node cur = iter.next();
                if (cur.key.equals(key)) {
                    iter.remove();
                    return cur.value;
                }
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        for (int i = 0; i < size; i++) {
            Iterator<Node> iter = buckets[i].iterator();
            while (iter.hasNext()) {
                Node cur = iter.next();
                if (cur.key.equals(key) && cur.value.equals(value)) {
                    iter.remove();
                    return cur.value;
                }
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}
