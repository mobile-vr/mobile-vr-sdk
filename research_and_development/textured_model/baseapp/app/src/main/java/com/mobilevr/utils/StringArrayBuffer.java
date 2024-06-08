package com.mobilevr.utils;

/**
 * Buffer holding String[].
 * Useful to hold log Strings for the virtual log window.
 */
public class StringArrayBuffer {
    private String[] buffer;
    public int size;
    private int currentIndex;
    public int maxCapacity;

    /**
     * Constructor to initialize the buffer with a given size and capacity
     *
     * @param size
     * @param maxCapacity
     */
    public StringArrayBuffer(int size, int maxCapacity) {
        this.size = size;
        this.maxCapacity = maxCapacity;
        this.buffer = new String[size];
        this.currentIndex = 0;
    }

    /**
     * Method to add a string to the buffer with overflow handling
     */
    public void add(String str) {
        while (str.length() > maxCapacity) {
            if (currentIndex < size) {
                buffer[currentIndex++] = str.substring(0, maxCapacity);
                str = str.substring(maxCapacity);
            } else {
                ensureCapacity();
                if (currentIndex < size) {
                    buffer[currentIndex++] = str.substring(0, maxCapacity);
                    str = str.substring(maxCapacity);
                }
            }
        }

        if (currentIndex < size) {
            buffer[currentIndex++] = str;
        } else {
            ensureCapacity();
            if (currentIndex < size) {
                buffer[currentIndex++] = str;
            }
        }

        str = null;
    }

    /**
     * Method to ensure the buffer doesn't exceed its capacity
     */
    private void ensureCapacity() {
        if (currentIndex >= size) {
            // Shift elements left to make space
            System.arraycopy(buffer, 1, buffer, 0, size - 1);
            buffer[size - 1] = null;
            currentIndex = size - 1;
        }
    }

    /**
     * Method to retrieve a string at a specific index
     *
     * @param index
     * @return
     */
    public String get(int index) {
        if (index >= 0 && index < currentIndex) {
            return buffer[index];
        } else {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
    }

    /**
     * Method to clear the buffer
     */
    public void clear() {
        buffer = new String[size];
        currentIndex = 0;
    }

    /**
     * Get the current size of the string buffer
     *
     * @return
     */
    public int getCurrentSize() {
        return currentIndex;
    }
}
