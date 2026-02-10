package bstmap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable, V> implements Map61B<K, V> {
    int size = 0;

    private class Node {
        Node(K k, V v, Node l, Node r) {
            key = k;
            val = v;
            left = l;
            right = r;
        }

        K key;
        V val;
        Node left;
        Node right;
    }

    private Node root;

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        Node cursor = null;
        Node next = root;
        int f = 2;
        size ++;
        if(containsKey(key)){
            cursor.val = value;
            return;
        }
        while (next != null) {
            cursor = next;
            if (key.compareTo(cursor.key) > 0) {
                next = next.right;
                f = 1;
            } else if (key.compareTo(cursor.key) < 0) {
                next = next.left;
                f = 0;
            }
        }
        next = new Node(key, value, null, null);
        if (f == 0) {
            cursor.left = next;
        } else if (f == 1) {
            cursor.right = next;
        } else {
            root = next;
        }
    }

    @Override
    public boolean containsKey(K key){
        Node cursor = root;
        while (cursor != null){
            if (key.compareTo(cursor.key) > 0) {
                cursor = cursor.right;
            } else if (key.compareTo(cursor.key) < 0) {
                cursor = cursor.left;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key){
        Node cursor = root;
        while (cursor != null){
            if (key.compareTo(cursor.key) > 0) {
                cursor = cursor.right;
            } else if (key.compareTo(cursor.key) < 0) {
                cursor = cursor.left;
            } else {
                return cursor.val;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }
}
