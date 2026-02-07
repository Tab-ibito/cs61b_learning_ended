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
        int target = 0;
        for (int i = 0; i < size(); i++) {
            if (cDefault.compare(get(target), get(i)) < 0) {
                target = i;
            }
        }
        return get(target);
    }

    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }
        int target = 0;
        for (int i = 0; i < size(); i++) {
            if (c.compare(get(target), get(i)) < 0) {
                target = i;
            }
        }
        return get(target);
    }
}
