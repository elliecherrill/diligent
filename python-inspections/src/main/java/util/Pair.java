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

    @Override
    public String toString() {
        return "(" + first.toString() + ", " + second.toString() + ")";
    }

    @Override
    public int hashCode() {
        return 11 * first.hashCode() + 71 * second.hashCode();
    }
}
