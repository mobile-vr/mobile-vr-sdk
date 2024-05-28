package com.mobilevr.log;

public class VirtualLogWindow {
    private int lineMaxChar, rowsMax;
    private int maxChar;
    private float charLength, charHeight;
    public float quadTopLeftX, quadTopLeftY, quadTopRightX, quadTopRightY, quadBotLeftX, quadBotLeftY,
            quadBotRightX, quadBotRightY;
    public float zPos;

    public VirtualLogWindow(int myLineMaxChar, int myRowsMax, float myZPos, float width, float height) {
        lineMaxChar = myLineMaxChar;
        rowsMax = myRowsMax;
        maxChar = lineMaxChar * rowsMax;
        zPos = myZPos;

        // compute length and height of character
        charLength = width / lineMaxChar;
        charHeight = height / rowsMax;

        // define the quad size
        quadTopLeftX = -charLength / 2;
        quadTopLeftY = charLength / 2;

        quadTopRightX = charLength / 2;
        quadTopRightY = charLength / 2;

        quadBotLeftX = -charLength / 2;
        quadBotLeftY = -charLength / 2;

        quadBotRightX = charLength / 2;
        quadBotRightY = -charLength / 2;

    }
}
