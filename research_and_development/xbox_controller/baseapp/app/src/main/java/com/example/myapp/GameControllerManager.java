package com.example.myapp;

import android.hardware.input.InputManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class GameControllerManager implements InputManager.InputDeviceListener {
    private String TAG="mobilevr";
    private final InputManager mInputManager;

    public GameControllerManager(InputManager inputManager) {
        mInputManager = inputManager;
    }

    public void startListening() {
        mInputManager.registerInputDeviceListener(this, null);
    }

    public void stopListening() {
        mInputManager.unregisterInputDeviceListener(this);
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        // Votre logique pour gérer les périphériques ajoutés
        Log.i(TAG, "New device added, ID: " + deviceId);
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        // Votre logique pour gérer les périphériques supprimés
        Log.i(TAG, "Device removed ID: " + deviceId);
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        // Votre logique pour gérer les périphériques modifiés
        Log.i(TAG, "Device modified, ID: " + deviceId);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Votre logique pour gérer les touches enfoncées
        Log.i(TAG, "Key down, keyCore: " + keyCode + " ; keyEvent : " + event);
        return true;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Votre logique pour gérer les touches relâchées
        Log.i(TAG, "Key up, keyCore: " + keyCode + " ; keyEvent : " + event);
        return true;
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        // Votre logique pour gérer les événements de mouvement génériques (analogiques)
        Log.i(TAG, "Generic motion Event: " + event);
        return true;
    }
}
