package com.mobilevr.log;

import com.mobilevr.utils.StringArrayBuffer;

/**
 * Class to hold information about the virtual log window, and the StringArrayBuffer.
 */
public class VirtualLogWindow {
    private int lineMaxChar, rowsMax;
    private int maxChar;
    private float charLength, charHeight;
    private float width, height;
    public float zPos;
    public StringArrayBuffer stringArrayBuffer;

    public VirtualLogWindow(int myLineMaxChar, int myRowsMax, float myZPos, float myWidth, float myHeight) {
        lineMaxChar = myLineMaxChar;
        rowsMax = myRowsMax;
        maxChar = lineMaxChar * rowsMax;
        zPos = myZPos;
        width = myWidth;
        height = myHeight;

        // compute length and height of character
        charLength = width / lineMaxChar;
        charHeight = height / rowsMax;

        stringArrayBuffer = new StringArrayBuffer(rowsMax - 1, lineMaxChar);

    }

    /**
     * This method adds a new string to the StringArrayBuffer.
     *
     * @param myString
     */
    public void add(String myString) {
        stringArrayBuffer.add(myString);
    }

    /**
     * Get the String at line i.
     *
     * @param i
     * @return
     */
    public String getString(int i) {
        return stringArrayBuffer.get(i);
    }

    public int getMaxChar() {
        return maxChar;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getZPos() {
        return zPos;
    }

    public int getLineMaxChar() {
        return lineMaxChar;
    }

    public float getCharLength() {
        return charLength;
    }

    public float getCharHeight() {
        return charHeight;
    }
}
