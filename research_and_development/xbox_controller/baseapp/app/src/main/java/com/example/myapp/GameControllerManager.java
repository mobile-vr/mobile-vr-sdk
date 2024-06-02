package com.example.myapp;

import android.hardware.input.InputManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.InputDevice;

import com.mobilevr.utils.BoundedStringBuffer;
import com.mobilevr.utils.StringArrayBuffer;

public class GameControllerManager implements InputManager.InputDeviceListener {
    private String TAG="mobilevr";
    private final InputManager mInputManager;
    //public BoundedStringBuffer debugString;
    public StringArrayBuffer stringArrayBuffer;
    // keycode :
    /*
        A : 96
        B : 97
        X : 99
        Y : 100
        Axis LeftTrigger (LT) : AXIS_LTRIGGER
        Axis RightTrigger (RT) : AXIS_RTRIGGER
        LeftShoulder (LB) : 102
        RightShoulder (RB) : 103
        start : 108
        select : 109
        AxisLeftX (vertical) : AXIS_X
        AxisLeftY (horizontal) : AXIS_Y
        AxisLeft : 106
        AxisRightX (vertical) : AXIS_Z
        AxisRightY (horizontal) : AXIS_RZ
        AxisRight : 107
        Guide (menu) : 110
        AXIS_HAT_X : (left to right) -1 to 1 (click down is 0.0, click up gives the value -1 or 1)
        AXIS_HAT_Y : (bottom to top) -1 to 1
     */

    public GameControllerManager(InputManager inputManager, /*int maxChar,*/ StringArrayBuffer myStringArrayBuffer) {

        mInputManager = inputManager;
        //debugString = new BoundedStringBuffer(maxChar);
        //stringArrayBuffer = new StringArrayBuffer(100, maxChar);
        stringArrayBuffer = myStringArrayBuffer;
    }

    public void startListening() {

        mInputManager.registerInputDeviceListener(this, null);
        stringArrayBuffer.add("start listening");
    }

    public void stopListening() {

        mInputManager.unregisterInputDeviceListener(this);
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        // Votre logique pour gérer les périphériques ajoutés
        Log.i(TAG, "New device added, ID: " + deviceId);
        stringArrayBuffer.add("New device added, ID: " + deviceId);
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        // Votre logique pour gérer les périphériques supprimés
        Log.i(TAG, "Device removed ID: " + deviceId);
        stringArrayBuffer.add("Device removed ID: " + deviceId);
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        // Votre logique pour gérer les périphériques modifiés
        Log.i(TAG, "Device modified, ID: " + deviceId);
        stringArrayBuffer.add("Device modified, ID: " + deviceId);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Votre logique pour gérer les touches enfoncées
        Log.i(TAG, "Key down, keyCode: " + keyCode + " ; keyEvent : " + event);
        stringArrayBuffer.add("Key down, keyCode: " + keyCode);// + " ; keyEvent : " + event);
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Votre logique pour gérer les touches relâchées
        Log.i(TAG, "Key up, keyCode: " + keyCode + " ; keyEvent : " + event);
        stringArrayBuffer.add("Key up, keyCode: " + keyCode);// + " ; keyEvent : " + event);
        return true;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        // Votre logique pour gérer les événements de mouvement génériques (analogiques)
        Log.i(TAG, "Generic motion Event: " + event);
        //debugString.add(" // Generic motion Event: " + event);
        handleJoystickInput(event);
        return true;
    }

    private void handleJoystickInput(MotionEvent event) {
        InputDevice device = event.getDevice();
        int pointerIndex = event.getActionIndex();
        int action = event.getActionMasked();

        // Process axis movements (e.g., joystick positions)
        if (action == MotionEvent.ACTION_MOVE) {
            for (InputDevice.MotionRange range : device.getMotionRanges()) {
                int axis = range.getAxis();
                float value = event.getAxisValue(axis, pointerIndex);

                String axisName = MotionEvent.axisToString(axis);

                //Log.d("JoystickInput", "Joystick: " + joystickName + ", Axis: " + axisName + ", Value: " + value);
                stringArrayBuffer.add("");
                stringArrayBuffer.add("Axis: " + axisName + ", Value: " + value);
            }
        }
    }
}
