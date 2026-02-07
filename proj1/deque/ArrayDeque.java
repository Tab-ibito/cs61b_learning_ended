package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size = 0;
    private int first;
    private int last;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        first = 4;
        last = 4;
    }

    private int[] resize(int capacity, boolean direction) {
        T[] a = (T[]) new Object[capacity + items.length];
        if (!direction) {
            if (last - first >= 0) {
                System.arraycopy(items, first, a, first + capacity, last - first);
            }
            items = a;
            return new int[]{first + capacity, last + capacity};
        } else {
            if (last - first >= 0) {
                System.arraycopy(items, first, a, first, last - first);
            }
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
        if (this == o) {
            return true;
        }
        // 依然检查是不是 Deque 接口
        if (!(o instanceof Deque)) {
            return false;
        }

        // 强制转换
        Deque<T> other = (Deque<T>) o;

        // 1. 检查大小
        if (this.size() != other.size()) {
            return false;
        }

        // 2. 用笨办法：for 循环 + get(i)
        // 这种写法不需要 other 拥有 iterator() 方法
        for (int i = 0; i < this.size(); i++) {
            T myItem = this.get(i);
            T otherItem = other.get(i);

            // 防御性编程：处理 null 的情况（虽然作业里通常不存 null）
            if (myItem == null) {
                if (otherItem != null) return false;
            } else {
                if (!myItem.equals(otherItem)) return false;
            }
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
