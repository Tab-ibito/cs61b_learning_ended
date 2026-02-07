package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> cDefault;

    public MaxArrayDeque(Comparator<T> c) {
        this.cDefault = c;
    }

    public T max() {
        if (size() == 0) {
            return null;
        }
        int target = first;
        for (int i = first; i < last; i++) {
            if (cDefault.compare(items[target], items[i]) < 0) {
                target = i;
            }
        }
        return items[target];
    }

    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }
        int target = first;
        for (int i = first; i < last; i++) {
            if (c.compare(items[target], items[i]) < 0) {
                target = i;
            }
        }
        return items[target];
    }
}
