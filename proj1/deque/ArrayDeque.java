package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T> {
    protected T[] items;
    private int size = 0;
    protected int first;
    protected int last;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        first = 4;
        last = 4;
    }

    private int[] resize(int capacity, boolean direction) {
        T[] a = (T[]) new Object[capacity + items.length];
        if (!direction) {
            if (last - first >= 0) System.arraycopy(items, first, a, first + capacity, last - first);
            items = a;
            return new int[]{first + capacity, last + capacity};
        } else {
            if (last - first >= 0) System.arraycopy(items, first, a, first, last - first);
            items = a;
            return new int[]{first, last};
        }
    }

    @Override
    public void addFirst(T item) {
        first--;
        items[first] = item;
        size++;
        if (first == 0) {
            int[] res = resize(size, false);
            first = res[0];
            last = res[1];
        }
    }

    @Override
    public void addLast(T item) {
        items[last] = item;
        last++;
        size++;
        if (last == items.length) {
            int[] res = resize(size, true);
            first = res[0];
            last = res[1];
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        for (int i = first; i < last; i++) {
            System.out.println(items[i] + " ");
        }
        System.out.println("\n");
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T removed = items[first];
        first++;
        size--;
        if (first > (int) (1.4 * size)) {
            int[] res = resize((int) (-0.4 * size), false);
            first = res[0];
            last = res[1];
        }
        return removed;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        last--;
        size--;
        T removed = items[last];
        if (items.length - last > (int) (1.4 * size)) {
            int[] res = resize((int) (-0.4 * size), true);
            first = res[0];
            last = res[1];
        }
        return removed;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int cursor = first;

            @Override
            public boolean hasNext() {
                return cursor < last;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T res = items[cursor];
                cursor++;
                return res;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        Iterator<T> cursor1 = this.iterator();
        Iterator<T> cursor2 = ((ArrayDeque<T>) o).iterator();
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

    @Override
    public T get(int index) {
        if (index + first >= last) {
            return null;
        }
        return items[index + first];
    }
}
