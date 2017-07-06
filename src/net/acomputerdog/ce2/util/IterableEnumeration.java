package net.acomputerdog.ce2.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IterableEnumeration<T> implements Iterable<T>, Enumeration<T> {
    private final Enumeration<T> enumeration;

    public IterableEnumeration(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return hasMoreElements();
            }

            @Override
            public T next() {
                return nextElement();
            }
        };
    }

    @Override
    public boolean hasMoreElements() {
        return enumeration != null && enumeration.hasMoreElements();
    }

    @Override
    public T nextElement() {
        return enumeration == null ? null : enumeration.nextElement();
    }
}
