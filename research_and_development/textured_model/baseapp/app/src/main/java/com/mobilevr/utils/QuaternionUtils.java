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

package com.mobilevr.utils;

import static com.mobilevr.utils.VectorUtils.transformVector;

import com.google.ar.core.Pose;

/**
 * Class to keep static methods concerning quaternions.
 */
public class QuaternionUtils {

    /**
     * Create a quaternion from Euler angles. This is used to know where the physical
     * camera is looking at in the virtual scene. You can't know where the detected finger
     * is pointing at using only the 2D position from the camera, you need to use the
     * intrinsic field of view of the camera.
     *
     * @param angleX: float angle in radians.
     * @param angleY: float angle in radians.
     * @param cameraPose: Pose of the virtual camera.
     * @return float[4]
     */
    public static float[] createQuaternionFromEulerAngles(float angleX, float angleY, Pose cameraPose) {
        // Convert the individual rotations to quaternions
        float[] xAxis = cameraPose.getXAxis();
        float[] yAxis = cameraPose.getYAxis();

        // Rotation around X axis
        float[] quaternionX = axisAngleToQuaternion(xAxis[0], xAxis[1], xAxis[2], angleX);
        // Rotation around Y axis
        float[] quaternionY = axisAngleToQuaternion(yAxis[0], yAxis[1], yAxis[2], angleY);

        normalizeQuaternion(quaternionX);
        normalizeQuaternion(quaternionY);

        return multiplyQuaternions(quaternionX, quaternionY);
    }

    /**
     * From a given rotation axis and an amount of degrees, this function returns a
     * quaternion.
     *
     * @param axisX: float
     * @param axisY: float
     * @param axisZ: float
     * @param angle: float in radians.
     * @return float[4]
     */
    public static float[] axisAngleToQuaternion(float axisX, float axisY, float axisZ, float angle) {
        float halfAngle = angle / 2.0f;
        float sinHalfAngle = (float) Math.sin(halfAngle);
        float cosHalfAngle = (float) Math.cos(halfAngle);

        float x = axisX * sinHalfAngle;
        float y = axisY * sinHalfAngle;
        float z = axisZ * sinHalfAngle;
        float w = cosHalfAngle;

        return new float[]{x, y, z, w};
    }

    /**
     * Multiply quaternions together.
     *
     * @param quaternion1: float[4]
     * @param quaternion2: float[4]
     * @return float[4]
     */
    public static float[] multiplyQuaternions(float[] quaternion1, float[] quaternion2) {
        float w1 = quaternion1[3];
        float x1 = quaternion1[0];
        float y1 = quaternion1[1];
        float z1 = quaternion1[2];

        float w2 = quaternion2[3];
        float x2 = quaternion2[0];
        float y2 = quaternion2[1];
        float z2 = quaternion2[2];

        float w = w1 * w2 - x1 * x2 - y1 * y2 - z1 * z2;
        float x = w1 * x2 + x1 * w2 + y1 * z2 - z1 * y2;
        float y = w1 * y2 - x1 * z2 + y1 * w2 + z1 * x2;
        float z = w1 * z2 + x1 * y2 - y1 * x2 + z1 * w2;

        return new float[]{x, y, z, w};
    }

    /**
     * Get the direction vector from the given quaternion.
     *
     * @param quaternion: float[4]
     * @return float[3]
     */
    public static float[] getDirectionVectorFromQuaternion(float[] quaternion) {
        float[] matrix = quaternionToMatrix(quaternion);
        float[] vector = {
                0.0f,
                0.0f,
                -1.0f
        };
        float[] directionVector = transformVector(vector, matrix);
        return directionVector;
    }

    /**
     * Normalize a quaternion.
     *
     * @param quaternion: float[4]
     */
    public static void normalizeQuaternion(float[] quaternion) {
        float magnitude = (float) Math.sqrt(quaternion[0] * quaternion[0] + quaternion[1] * quaternion[1] +
                quaternion[2] * quaternion[2] + quaternion[3] * quaternion[3]);

        if (magnitude != 0.0f) {
            for (int i = 0; i < 4; i++) {
                quaternion[i] /= magnitude;
            }
        }
    }

    /**
     * Returns the rotation matrix of a quaternion.
     *
     * @param quaternion: float[4]
     * @return float[16]
     */
    public static float[] quaternionToMatrix(float[] quaternion) {
        float[] matrix = new float[16];
        float[] finalMatrix = new float[16];
        // Extract the quaternion components
        float x = quaternion[0];
        float y = quaternion[1];
        float z = quaternion[2];
        float w = quaternion[3];

        // Calculate the matrix elements
        float xx = x * x;
        float xy = x * y;
        float xz = x * z;
        float xw = x * w;
        float yy = y * y;
        float yz = y * z;
        float yw = y * w;
        float zz = z * z;
        float zw = z * w;

        // Column 1
        matrix[0] = 1 - 2 * (yy + zz);
        matrix[1] = 2 * (xy - zw);
        matrix[2] = 2 * (xz + yw);
        matrix[3] = 0;

        // Column 2
        matrix[4] = 2 * (xy + zw);
        matrix[5] = 1 - 2 * (xx + zz);
        matrix[6] = 2 * (yz - xw);
        matrix[7] = 0;

        // Column 3
        matrix[8] = 2 * (xz - yw);
        matrix[9] = 2 * (yz + xw);
        matrix[10] = 1 - 2 * (xx + yy);
        matrix[11] = 0;

        // Column 4 (translation)
        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = 0;
        matrix[15] = 1;

        // Get transpose matrix
        finalMatrix[0] = matrix[0];
        finalMatrix[1] = matrix[4];
        finalMatrix[2] = matrix[8];
        finalMatrix[3] = matrix[12];

        finalMatrix[4] = matrix[1];
        finalMatrix[5] = matrix[5];
        finalMatrix[6] = matrix[9];
        finalMatrix[7] = matrix[13];

        finalMatrix[8] = matrix[2];
        finalMatrix[9] = matrix[6];
        finalMatrix[10] = matrix[10];
        finalMatrix[11] = matrix[14];

        finalMatrix[12] = matrix[3];
        finalMatrix[13] = matrix[7];
        finalMatrix[14] = matrix[11];
        finalMatrix[15] = matrix[15];

        return finalMatrix;
    }

    /**
     * Convert a quaternion to Euler angles in radians or degrees.
     *
     * @param quaternion: float[4]
     * @param mode: String "RADIANS" or "DEGREES".
     * @return float[3]
     */
    public static float[] convertQuaternionToEulerAngles(float[] quaternion, String mode) {
        //Quaternion quaternion = new Quaternion(x, y, z, w);

        // Convert the quaternion to Euler angles
        float[] eulerAngles = new float[3];

        if (mode == "DEGREES") {

            // Roll (x-axis rotation)
            eulerAngles[0] = (float) Math.toDegrees(Math.atan2(2 * (quaternion[3] * quaternion[0] + quaternion[1] * quaternion[2]), 1 - 2 * (quaternion[0] * quaternion[0] + quaternion[1] * quaternion[1])));

            // Pitch (y-axis rotation)
            eulerAngles[1] = (float) Math.toDegrees(Math.asin(2 * (quaternion[3] * quaternion[1] - quaternion[2] * quaternion[0])));

            // Yaw (z-axis rotation)
            eulerAngles[2] = (float) Math.toDegrees(Math.atan2(2 * (quaternion[3] * quaternion[2] + quaternion[0] * quaternion[1]), 1 - 2 * (quaternion[1] * quaternion[1] + quaternion[2] * quaternion[2])));

        } else if (mode == "RADIANS") {
            // Roll (x-axis rotation)
            eulerAngles[0] = (float) Math.atan2(2 * (quaternion[3] * quaternion[0] + quaternion[1] * quaternion[2]), 1 - 2 * (quaternion[0] * quaternion[0] + quaternion[1] * quaternion[1]));

            // Pitch (y-axis rotation)
            eulerAngles[1] = (float) Math.asin(2 * (quaternion[3] * quaternion[1] - quaternion[2] * quaternion[0]));

            // Yaw (z-axis rotation)
            eulerAngles[2] = (float) Math.atan2(2 * (quaternion[3] * quaternion[2] + quaternion[0] * quaternion[1]), 1 - 2 * (quaternion[1] * quaternion[1] + quaternion[2] * quaternion[2]));
        }
        return eulerAngles;
    }
}
