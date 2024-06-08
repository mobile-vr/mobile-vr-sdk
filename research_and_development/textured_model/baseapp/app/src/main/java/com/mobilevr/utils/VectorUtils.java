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

/**
 * Class to keep static methods for vectors.
 */
public class VectorUtils {
    /**
     * A and B two vectors, this method returns A - B
     *
     * @param A: float[3]
     * @param B: float[3]
     * @return float[3]
     */
    public static float[] vectorSubtraction(float[] A, float[] B) {
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = A[i] - B[i];
        }
        return result;
    }

    /**
     * Apply the rotation and translation contained in the Pose matrix to the given 3D vector.
     * i3, i7, i11, i15 are ignored.
     *
     * Do: Matrix x Vector
     *
     * |  i0  i4  i8 i12 |   (   x   )      ( transformed X )
     * |  i1  i5  i9 i13 |   (   y   )      ( transformed Y )
     * |  i2  i6 i10 i14 |   (   z   )      ( transformed Z )
     * |  i3  i7 i11 i15 | * (   1   )   =  (      1        )
     *
     * @param vector float[3]
     * @param matrix float[16]
     * @return transformed vector.
     */
    public static float[] transformVector(float[] vector, float[] matrix) {
        if (vector.length != 3 || matrix.length != 16) {
            throw new IllegalArgumentException("Vector and matrix dimensions are not compatible.");
        }

        float x = vector[0];
        float y = vector[1];
        float z = vector[2];

        float transformedX = matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12];
        float transformedY = matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13];
        float transformedZ = matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14];

        return new float[]{transformedX, transformedY, transformedZ};
    }

    /**
     * Transform a list of vertices containing only the 3D position according to the given
     * Pose matrix.
     *
     * @param vertices float[i x 3]
     * @param matrix float[16]
     * @return the array of transformed vertices.
     */
    public static float[] transformVertices(float[] vertices, float[] matrix) {
        float[] transformedVertices = new float[vertices.length];
        float[] vector = new float[3];
        float[] transformedVector = new float[3];
        int numberOfVertices = vertices.length / 3;

        for (int i=0 ; i<numberOfVertices ; i++) {
            vector[0] = vertices[i*3];
            vector[1] = vertices[i*3+1];
            vector[2] = vertices[i*3+2];

            transformedVector = transformVector(vector, matrix);

            transformedVertices[i*3] = transformedVector[0];
            transformedVertices[i*3+1] = transformedVector[1];
            transformedVertices[i*3+2] = transformedVector[2];
        }

        return transformedVertices;
    }

    /**
     * Do the cross product between the 3D vectors A and B.
     *
     * @param A: float[3]
     * @param B: float[3]
     * @return float[3]
     */
    public static float[] crossProduct(float[] A, float[] B) {
        float[] result = new float[3];
        result[0] = A[1] * B[2] - A[2] * B[1];
        result[1] = A[2] * B[0] - A[0] * B[2];
        result[2] = A[0] * B[1] - A[1] * B[0];
        return result;
    }

    /**
     * Calculate the norm of the vector.
     *
     * @param vector: float[]
     * @return float.
     */
    public static float norm(float[] vector) {
        float norm;
        float sum=0;
        for (int i=0;i<vector.length;i++) {
            sum += (float) Math.pow(vector[i], 2);
        }
        norm = (float) Math.sqrt( sum );
        return norm;
    }

    /**
     * Apply a rotation to a 2D vector in counterclockwise (trigonometric).
     *
     * @param vector: 2D vector to rotate.
     * @param alpha: angle in radians.
     * @return rotated angle.
     */
    public static float[] rotate(float[] vector, double alpha) {
        double cosAlpha = Math.cos(alpha);
        double sinAlpha = Math.sin(alpha);

        // Perform the rotation
        double newX = vector[0] * cosAlpha - vector[1] * sinAlpha;
        double newY = vector[0] * sinAlpha + vector[1] * cosAlpha;

        // Update the vector's coordinates
        vector[0] = (float) newX;
        vector[1] = (float) newY;

        return vector;
    }

    /**
     * Function to rotate a 3D vector around a given axis by a specified angle.
     *
     * @param axis is normalized in the function.
     * @param angle in radians
     * @return the rotated float[3] vector.
     */
    public static float[] rotateVector3D(float[] vector, float[] axis, float angle) {
        float cosTheta = (float) Math.cos(angle);
        float sinTheta = (float) Math.sin(angle);

        // Normalize the rotation axis
        float[] normalizedAxis = normalizeVector(axis);

        // Calculate the rotation matrix elements
        float[][] rotationMatrix = {
                {cosTheta + normalizedAxis[0] * normalizedAxis[0] * (1 - cosTheta),
                        normalizedAxis[0] * normalizedAxis[1] * (1 - cosTheta) - normalizedAxis[2] * sinTheta,
                        normalizedAxis[0] * normalizedAxis[2] * (1 - cosTheta) + normalizedAxis[1] * sinTheta},
                {normalizedAxis[1] * normalizedAxis[0] * (1 - cosTheta) + normalizedAxis[2] * sinTheta,
                        cosTheta + normalizedAxis[1] * normalizedAxis[1] * (1 - cosTheta),
                        normalizedAxis[1] * normalizedAxis[2] * (1 - cosTheta) - normalizedAxis[0] * sinTheta},
                {normalizedAxis[2] * normalizedAxis[0] * (1 - cosTheta) - normalizedAxis[1] * sinTheta,
                        normalizedAxis[2] * normalizedAxis[1] * (1 - cosTheta) + normalizedAxis[0] * sinTheta,
                        cosTheta + normalizedAxis[2] * normalizedAxis[2] * (1 - cosTheta)}
        };

        // Multiply the rotation matrix by the original vector to get the rotated vector
        float[] rotatedVector = new float[3];
        for (int i = 0; i < 3; i++) {
            rotatedVector[i] = 0;
            for (int j = 0; j < 3; j++) {
                rotatedVector[i] += rotationMatrix[i][j] * vector[j];
            }
        }

        return rotatedVector;
    }

    /**
     * Normalize a given 3D vector.
     *
     * @param vector float[3]
     * @return float[3]
     */
    public static float[] normalizeVector(float[] vector) {
        float length = (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        float[] normalizedVector = {vector[0] / length, vector[1] / length, vector[2] / length};
        return normalizedVector;
    }

    /**
     * Concatenate arrays into one array. Very useful to concatenate vertices.
     *
     * @param arrays: float[], float[], float[] ...
     * @return a single float[]
     */
    public static float[] concatenateArrays(float[]... arrays) {
        // Calculate the total length of the concatenated array
        int totalLength = 0;
        for (float[] array : arrays) {
            totalLength += array.length;
        }

        // Create a new array with the combined length
        float[] result = new float[totalLength];

        // Copy elements from each array
        int currentIndex = 0;
        for (float[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }
}
