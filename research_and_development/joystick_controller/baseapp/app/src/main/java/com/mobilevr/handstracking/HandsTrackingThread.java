/*
 * Copyright 2024 MasterHansCoding (GitHub)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mobilevr.handstracking;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Thread that runs a hands tracking program.
 */
public class HandsTrackingThread extends Thread {
    private static final String TAG = "vrapp";
    private long time;
    public long latency=0;
    private Hands hands;
    private static final boolean RUN_ON_GPU = true;
    private List<LandmarkProto.NormalizedLandmark> landmarkList;
    public Boolean isRunning=true;
    private Context appContext;
    private String txt;
    private Boolean newCameraImage=false;
    private byte[] imageDataY, imageDataU, imageDataV;
    private int width, height;
    private int pixelStrideY, pixelStrideU, pixelStrideV;
    private int rowStrideY, rowStrideU, rowStrideV;
    private int[] argbArray;
    private long timerNoHands;
    private Boolean timerNoHandsStarted;

    /**
     * Init the Thread.
     *
     * @param myAppContext: Context of an Activity.
     */
    public HandsTrackingThread(Context myAppContext) {
        appContext = myAppContext;
        time = System.currentTimeMillis();
        setupStaticImageModePipeline();
        timerNoHandsStarted = false;
    }

    @Override
    public void run() {

        Log.i(TAG, "Hello handsTrackingThread!");

        while (true) {
            if (newCameraImage && isRunning) {
                int r, g, b;
                int yValue, uValue, vValue;

                for (int y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {
                        int yIndex = (y * rowStrideY) + (x * pixelStrideY);
                        // Y plane should have positive values belonging to [0...255]
                        yValue = (imageDataY[yIndex] & 0xff);

                        int uvx = x / 2;
                        int uvy = y / 2;
                        // U/V Values are subsampled i.e. each pixel in U/V chanel in a
                        // YUV_420 image act as chroma value for 4 neighbouring pixels
                        int uvIndex = (uvy * rowStrideU) + (uvx * pixelStrideU);

                        // U/V values ideally fall under [-0.5, 0.5] range. To fit them into
                        // [0, 255] range they are scaled up and centered to 128.
                        // Operation below brings U/V values to [-128, 127].
                        uValue = (imageDataU[uvIndex] & 0xff) - 128;
                        vValue = (imageDataV[uvIndex] & 0xff) - 128;

                        // Compute RGB values per formula above.
                        r = (int) (yValue + 1.370705f * vValue);
                        g = (int) (yValue - (0.698001f * vValue) - (0.337633f * uValue));
                        b = (int) (yValue + 1.732446f * uValue);
                        r = clamp(r, 0, 255);
                        g = clamp(g, 0, 255);
                        b = clamp(b, 0, 255);

                        // Use 255 for alpha value, no transparency. ARGB values are
                        // positioned in each byte of a single 4 byte integer
                        // [AAAAAAAARRRRRRRRGGGGGGGGBBBBBBBB]
                        int argbIndex = y * width + x;
                        argbArray[argbIndex]
                                = (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
                    }
                }
                Bitmap bitmap = Bitmap.createBitmap(argbArray, width, height, Bitmap.Config.ARGB_8888);
                try {
                    hands.send(bitmap);
                } catch (Error ignored) {
                    // catch error yourself with >>adb logcat --buffer=crash
                }

                newCameraImage = false;
            }
        }
    }

    /**
     * Clamp function. Returns the value if it is between the min and max, or it returns
     * the min or max.
     *
     * @param value: int
     * @param min: int
     * @param max: int
     * @return int
     */
    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * Setup the static image mode pipeline. It initiates Hands and the result callback.
     */
    private void setupStaticImageModePipeline() {
        // Initializes a new MediaPipe Hands solution instance in the static image mode.
        hands =
                new Hands(
                        appContext,
                        HandsOptions.builder()
                                .setStaticImageMode(true)
                                .setMaxNumHands(2)
                                .setRunOnGpu(RUN_ON_GPU)
                                .build());

        // Connects MediaPipe Hands solution to the user-defined HandsResultImageView.
        hands.setResultListener(this::logWristLandmark);
        //hands.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Hands error:" + message));
    }

    /**
     * Actualize landmarkList attribute while the Machine Learning model is
     * detecting hands.
     *
     * @param result
     */
    private void logWristLandmark(HandsResult result) {
        /**
         * FYI:
         *     public static final int NUM_LANDMARKS = 21;
         *     public static final int WRIST = 0;
         *     public static final int THUMB_CMC = 1;
         *     public static final int THUMB_MCP = 2;
         *     public static final int THUMB_IP = 3;
         *     public static final int THUMB_TIP = 4;
         *     public static final int INDEX_FINGER_MCP = 5;
         *     public static final int INDEX_FINGER_PIP = 6;
         *     public static final int INDEX_FINGER_DIP = 7;
         *     public static final int INDEX_FINGER_TIP = 8;
         *     public static final int MIDDLE_FINGER_MCP = 9;
         *     public static final int MIDDLE_FINGER_PIP = 10;
         *     public static final int MIDDLE_FINGER_DIP = 11;
         *     public static final int MIDDLE_FINGER_TIP = 12;
         *     public static final int RING_FINGER_MCP = 13;
         *     public static final int RING_FINGER_PIP = 14;
         *     public static final int RING_FINGER_DIP = 15;
         *     public static final int RING_FINGER_TIP = 16;
         *     public static final int PINKY_MCP = 17;
         *     public static final int PINKY_PIP = 18;
         *     public static final int PINKY_DIP = 19;
         *     public static final int PINKY_TIP = 20;
         */
        if (result.multiHandLandmarks().isEmpty()) {
            if (!timerNoHandsStarted) {
                timerNoHands = System.currentTimeMillis();
                timerNoHandsStarted = true;
            }
            if (System.currentTimeMillis() - timerNoHands > 1000) {
                //Log.i(TAG, "1s without hands detected");
                landmarkList = null;
                timerNoHandsStarted = false;
            }
            return;
        } else {
            timerNoHandsStarted = false;
        }
        LandmarkProto.NormalizedLandmark wristLandmark =
                result.multiHandLandmarks().get(0).getLandmarkList().get(HandLandmark.INDEX_FINGER_TIP);
        landmarkList = result.multiHandLandmarks().get(0).getLandmarkList();
        txt = "x : " + Float.toString(wristLandmark.getX()) + " " +
                " y : " + Float.toString(wristLandmark.getY()) + " " +
                " z : " + Float.toString(wristLandmark.getZ());
    }

    /**
     * Get the txt which contains a string of the x, y, z values of the detected finger
     * tip.
     *
     * @return String
     */
    public String getLandmark() {
        return txt;
    }

    /**
     * Get the landmarkList of all the detected hand's parts.
     *
     * @return List<LandmarkProto.NormalizedLandmark>
     */
    public  List<LandmarkProto.NormalizedLandmark> getLandmarkList() {
        return landmarkList;
    }

    /**
     * onResume Thread method.
     */
    public void onResume() {
        isRunning = true;
    }

    /**
     * onPause Thread method.
     */
    public void onPause() {
        isRunning = false;
    }

    /**
     * Set a new camera image to this Thread so that it detects the hand's parts positions.
     *
     * @param cameraImage: Image
     */
    public void setCameraImage(Image cameraImage) {
        if (!newCameraImage) {
            // get buffers data
            ByteBuffer bufferY = cameraImage.getPlanes()[0].getBuffer();
            imageDataY = new byte[bufferY.remaining()];
            bufferY.get(imageDataY);

            ByteBuffer bufferU = cameraImage.getPlanes()[1].getBuffer();
            imageDataU = new byte[bufferU.remaining()];
            bufferU.get(imageDataU);

            ByteBuffer bufferV = cameraImage.getPlanes()[2].getBuffer();
            imageDataV = new byte[bufferV.remaining()];
            bufferV.get(imageDataV);

            // ARGB array needed by Bitmap static factory method I use below.
            width = cameraImage.getWidth();
            height = cameraImage.getHeight();
            argbArray = new int[width * height];

            pixelStrideY = cameraImage.getPlanes()[0].getPixelStride();
            pixelStrideU = cameraImage.getPlanes()[1].getPixelStride();
            pixelStrideV = cameraImage.getPlanes()[2].getPixelStride();
            rowStrideY = cameraImage.getPlanes()[0].getRowStride();
            rowStrideU = cameraImage.getPlanes()[1].getRowStride();
            rowStrideV = cameraImage.getPlanes()[2].getRowStride();


            newCameraImage = true;
            latency = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
        }
    }
}
