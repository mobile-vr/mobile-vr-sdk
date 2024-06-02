package com.mobilevr.log;

import com.mobilevr.utils.BoundedStringBuffer;
import com.mobilevr.utils.StringArrayBuffer;

public class VirtualLogWindow {
    //private String logString;
    private int lineMaxChar, rowsMax;
    private int maxChar;
    private float charLength, charHeight;
    private float width, height;
    public float zPos;
    //private BoundedStringBuffer boundedStringBuffer;
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

        //boundedStringBuffer = new BoundedStringBuffer(maxChar);
        stringArrayBuffer = new StringArrayBuffer(rowsMax - 1, lineMaxChar);

    }

    public void add(String myString) {
        // reverse the string then setLength to cut the old part
        /*logString = new StringBuilder(myString)
                .reverse()
                .toString();

        if (logString.length() > maxChar) {
            logString = logString.substring(0, maxChar);
        }

        // reverse back the string
        logString = new StringBuilder(logString)
                .reverse()
                .toString();*/

        //boundedStringBuffer.add(myString);
        stringArrayBuffer.add(myString);
    }

    public String getString(int i) {
        //return boundedStringBuffer.getBuffer();
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
