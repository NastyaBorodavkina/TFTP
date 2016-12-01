package misc;

import java.util.Iterator;

public class ArrayIterator<T> implements Iterator<T> {
    public T[] array;
    public int begin;
    public int end;

    public ArrayIterator(T[] array) {
        this.array = array;
        this.begin = 0;
        this.end = array.length;
    }

    public ArrayIterator(T[] array, int begin, int end) {
        this.array = array;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public boolean hasNext() {
        return begin < end;
    }

    @Override
    public T next() {
        return array[begin++];
    }
}
