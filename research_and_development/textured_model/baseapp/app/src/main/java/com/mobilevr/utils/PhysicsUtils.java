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

public class PhysicsUtils {
    /**
     * Calculate the position after a certain amount of time, of an object with
     * a given mass, in a given gravity field.
     *
     * @param initialPosition: float[3] initial position of the 0.1kg object.
     * @param initialSpeed: float[3] initial speed of the object.
     * @param time: float time in seconds.
     * @param mass: float in kg.
     * @param gravity: float[3] in m/s^2
     * @return: the new position.
     */
    public static float[] calculatePosition(
            float[] initialPosition,
            float[] initialSpeed,
            float time,
            float mass,
            float[] gravity
    ) {
        if (initialPosition.length != 3 || initialSpeed.length != 3) {
            throw new IllegalArgumentException("Input vectors must be of length 3.");
        }

        float[] newPosition = new float[3];
        for (int i = 0; i < 3; i++) {
            // Use the equation: newPosition = initialPosition + (initialSpeed * time)
            newPosition[i] = initialPosition[i] + initialSpeed[i] * time + 0.5f * gravity[i] * mass * time * time;
        }

        return newPosition;
    }

    /**
     * Calculate the new speed after a collision with a plan defined by a normal vector.
     *
     * @param initialSpeed: float[3], the speed before the collision.
     * @param normalVector: float[3], the normal vector of the plan.
     * @param COR: float, coefficient of restitution for the bouncing effect. The COR is
     *           between 0 and 1, with 1 representing a highly elastic bounce and 0
     *           representing a completely inelastic collision.
     * @return float[3], the speed after collision.
     */
    public static float[] calculateNewSpeed(float[] initialSpeed, float[] normalVector, float COR) {
        if (initialSpeed.length != 3 || normalVector.length != 3) {
            throw new IllegalArgumentException("Both vectors must be 3D vectors (float[3]).");
        }

        float dotProduct = 0;
        for (int i = 0; i < 3; i++) {
            dotProduct += initialSpeed[i] * normalVector[i];
        }

        float[] newSpeed = new float[3];
        for (int i = 0; i < 3; i++) {
            newSpeed[i] = initialSpeed[i] - (1 + COR) * dotProduct * normalVector[i];
        }

        return newSpeed;
    }
}
