package info.kgeorgiy.ja.boguslavskaya.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {
    private final List<E> data;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends E> dataCollection) {
        this(dataCollection, null);
    }

    public ArraySet(Collection<? extends E> dataCollection, Comparator<? super E> comparator) {
        TreeSet<E> set = new TreeSet<>(comparator);
        set.addAll(dataCollection);
        this.data = new ArrayList<>(set);
        this.comparator = comparator;
    }

    private ArraySet(List<E> list, Comparator<? super E> comparator) {
        this.data = list;
        this.comparator = comparator;
    }

    private int indexInSet(E element) {
        int res = Collections.binarySearch(data, element, comparator);
        if (res < 0) {
            return -1 - res;
        }
        return res;
    }

    private ArraySet<E> createSubSet(int startIndex, int endIndex) {
        return new ArraySet<>(data.subList(startIndex, endIndex), comparator);
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (E) o, comparator) >= 0;
    }

    @Override
    public Comparator<? super E> comparator() {
        return this.comparator;
    }


    @SuppressWarnings("unchecked")
    private int compare(E first, E second) {
        if (comparator != null) {
            return comparator.compare(first, second);
        } else {
            return ((Comparable<E>) first).compareTo(second);
        }
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return createSubSet(indexInSet(fromElement), indexInSet(toElement));
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return createSubSet(0, indexInSet(toElement));
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return createSubSet(indexInSet(fromElement), size());
    }

    @Override
    public E first() {
        checkIfIsEmpty();
        return data.get(0);
    }

    @Override
    public E last() {
        checkIfIsEmpty();
        return data.get(size() - 1);
    }

    private void checkIfIsEmpty() {
        if (size() == 0) {
            throw new NoSuchElementException();
        }
    }
}
