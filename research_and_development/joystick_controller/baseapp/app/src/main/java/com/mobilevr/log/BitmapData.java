package com.mobilevr.log;

public class BitmapData {
    private byte[] data;
    private int width;
    private int height;

    public BitmapData(byte[] data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    public byte[] getData() {
        return data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
