package adris.altoclef.util;

/**
 * Replacement for the removed net.minecraft.util.Pair.
 * Immutable left/right tuple.
 */
public class Pair<L, R> {
    private final L _left;
    private final R _right;

    public Pair(L left, R right) {
        _left = left;
        _right = right;
    }

    public L getLeft() {
        return _left;
    }

    public R getRight() {
        return _right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return (_left == null ? pair._left == null : _left.equals(pair._left))
            && (_right == null ? pair._right == null : _right.equals(pair._right));
    }

    @Override
    public int hashCode() {
        int result = _left == null ? 0 : _left.hashCode();
        result = 31 * result + (_right == null ? 0 : _right.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "(" + _left + ", " + _right + ")";
    }
}
