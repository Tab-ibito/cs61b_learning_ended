package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    class Node {
        private T val;
        private Node next = null;
        private Node pre = null;

        private Node(T item) {
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
        first = null;
        last = null;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        if (isEmpty()) {
            first = new Node(item);
            last = first;
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
            first = new Node(item);
            last = first;
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
                return size != 0 || cursor != null;
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
