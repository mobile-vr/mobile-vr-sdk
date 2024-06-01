package com.mobilevr.utils;

public class BoundedStringBuffer {
    private StringBuffer buffer;
    private int maxCapacity;

    public BoundedStringBuffer(int maxCapacity) {
        this.buffer = new StringBuffer();
        this.maxCapacity = maxCapacity;
    }

    public synchronized void add(String str) {
        buffer.append(str);
        ensureCapacity();
    }

    public synchronized void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        ensureCapacity();
    }

    public synchronized String getBuffer() {
        return buffer.toString();
    }

    public synchronized int getMaxCapacity() {
        return maxCapacity;
    }

    private void ensureCapacity() {
        if (buffer.length() > maxCapacity) {
            buffer.delete(0, buffer.length() - maxCapacity);
        }
    }
}

