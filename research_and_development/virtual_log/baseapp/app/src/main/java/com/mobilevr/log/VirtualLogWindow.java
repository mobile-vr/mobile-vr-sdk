package com.mobilevr.log;

public class VirtualLogWindow {
    private String logString;
    private int lineMaxChar, rowsMax;
    private int maxChar;
    private float charLength, charHeight;
    private float width, height;
    public float zPos;

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

    }

    public void setString(String myString) {
        // reverse the string then setLength to cut the old part
        logString = new StringBuilder(myString)
                .reverse()
                .toString();

        if (logString.length() > maxChar) {
            logString = logString.substring(0, maxChar);
        }

        // reverse back the string
        logString = new StringBuilder(logString)
                .reverse()
                .toString();
    }

    public String getString() {
        return logString;
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
