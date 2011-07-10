package com.github.ignition.support;

import java.lang.reflect.Array;

public class ArraySupport {

    @SuppressWarnings("unchecked")
    public static <T> T[] join(T[] head, T[] tail) {
        if (head == null) {
            return tail;
        }
        if (tail == null) {
            return head;
        }
        Class<?> type = head.getClass().getComponentType();
        T[] result = (T[]) Array.newInstance(type, head.length + tail.length);

        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(tail, 0, result, head.length, tail.length);

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] delete(T[] array, int index) {
        int length = array.length;
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }

        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1) {
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        }

        return result;
    }

    /**
     * Attempts to find an object in a potentially unsorted array. Complexity is
     * O(N).
     * 
     * @param <T>
     * @param array
     *        the input array
     * @param object
     *        the object to find
     * @return the index of the object if found, -1 otherwise.
     */
    public static <T> int find(T[] array, T object) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(object)) {
                return i;
            }
        }
        return -1;
    }
}
