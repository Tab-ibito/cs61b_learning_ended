package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T> {
    class Node {
        private T val;
        private Node next = null;
        private Node pre = null;

        Node(T item) {
            val = item;
        }

        public T val() {
            return val;
        }

        public Node next() {
            return next;
        }

        public Node pre() {
            return pre;
        }
    }

    private int size;
    private Node first;
    private Node last;

    public LinkedListDeque() {
        first = new Node(null);
        last = first;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        if (isEmpty()) {
            first.val = item;
        } else {
            Node added = new Node(item);
            added.next = first;
            first.pre = added;
            first = added;
        }
        size += 1;
    }

    @Override
    public void addLast(T item) {
        if (isEmpty()) {
            last.val = item;
        } else {
            Node added = new Node(item);
            added.pre = last;
            last.next = added;
            last = added;
        }
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node p = first;
        while (p != null) {
            System.out.println(p.val() + " ");
            p = p.next;
        }
        System.out.println("\n");
    }

    @Override
    public T removeFirst() {
        if (size > 0) {
            T removed = first.val();
            first = first.next;
            if (first != null) {
                first.pre = null;
            }
            size -= 1;
            return removed;
        }
        return null;
    }

    @Override
    public T removeLast() {
        if (size > 0) {
            T removed = last.val();
            last = last.pre;
            if (last != null) {
                last.next = null;
            }
            size -= 1;
            return removed;
        }
        return null;
    }

    @Override
    public T get(int index) {
        Node p = first;
        int i = 0;
        while (i < index) {
            p = p.next;
            i++;
        }
        return p.val();
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node cursor = first;

            @Override
            public boolean hasNext() {
                return cursor != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T res = cursor.val();
                cursor = cursor.next;
                return res;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LinkedListDeque)) {
            return false;
        }
        Iterator<T> cursor1 = this.iterator();
        Iterator<T> cursor2 = ((LinkedListDeque<T>) o).iterator();
        while (cursor1.hasNext() && cursor2.hasNext()) {
            T a = cursor1.next();
            T b = cursor2.next();
            if (!a.equals(b)) {
                return false;
            }
        }
        if (cursor1.hasNext() || cursor2.hasNext()) {
            return false;
        }
        return true;
    }

    private T runner(Node f, int n) {
        if (n == 0) {
            return f.val();
        }
        return runner(f.next, n - 1);
    }

    public T getRecursive(int index) {
        return runner(first, index);
    }
}
