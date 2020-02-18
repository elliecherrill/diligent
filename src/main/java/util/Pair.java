package util;

public class Pair<T, S> {
    private final T first;
    private final S second;

    public Pair(T firstElem, S secondElem) {
        first = firstElem;
        second = secondElem;
    }

    public T getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }
}
