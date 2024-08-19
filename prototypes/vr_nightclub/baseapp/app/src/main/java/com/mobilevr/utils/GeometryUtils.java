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

import static com.mobilevr.utils.QuaternionUtils.createQuaternionFromEulerAngles;
import static com.mobilevr.utils.QuaternionUtils.getDirectionVectorFromQuaternion;
import static com.mobilevr.utils.QuaternionUtils.multiplyQuaternions;
import static com.mobilevr.utils.VectorUtils.crossProduct;
import static com.mobilevr.utils.VectorUtils.norm;
import static com.mobilevr.utils.VectorUtils.vectorSubtraction;

import android.util.Log;

import com.google.ar.core.Pose;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.mobilevr.handstracking.IntersectionPoint;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import java.util.List;

/**
 * To keep geometry static methods.
 */
public class GeometryUtils {

    /**
     * Linear interpolation function to find the point at a given y.,
     * between 3D points 1 and 2.
     *
     * @param point1: float[3]
     * @param point2: float[3]
     * @param targetY: float
     * @return : float[3]
     */
    public static float[] interpolatePoint(float[] point1, float[] point2, float targetY) {
        // Ensure y1 is less than y2
        if (point1[1] > point2[1]) {
            float[] temp = point1;
            point1 = point2;
            point2 = temp;
        }

        // Calculate the interpolation factor based on y coordinates
        float t = (targetY - point1[1]) / (point2[1] - point1[1]);

        // Interpolate x, y, and z coordinates separately
        float interpolatedX = point1[0] + t * (point2[0] - point1[0]);
        float interpolatedY = targetY;
        float interpolatedZ = point1[2] + t * (point2[2] - point1[2]);

        return new float[]{interpolatedX, interpolatedY, interpolatedZ};
    }

    /**
     * To get the quaternion of the direction pointed by the finger in front of the camera.
     * This depends on the intrinsic parameters of the physical camera which are the fovx and fovy.
     *
     * @param cameraPose: (Pose) the Pose of the camera (from camera.getPose())
     * @param landmarkList: (List<LandmarkProto.NormalizedLandmark>)
     * @param fovx: (float)
     * @param fovy: (float)
     * @return fingerQuaternion: (float[4])
     * @throws NullPointerException
     */
    public static float[] getFingerQuaternion(
            Pose cameraPose,
            List<LandmarkProto.NormalizedLandmark> landmarkList,
            float fovx,
            float fovy) throws NullPointerException
    {
        if (cameraPose == null) {
            throw new NullPointerException("cameraPose is null");
        }
        if (landmarkList == null) {
            throw new NullPointerException("landmarkList is null");
        }
        // Direction pointed by the hand in camera's coordinate system
        float myX = landmarkList.get(HandLandmark.INDEX_FINGER_TIP).getX();
        float myY = landmarkList.get(HandLandmark.INDEX_FINGER_TIP).getY();
        float myX2 = myX - 0.5f;
        float myY2 = -myY + 0.5f;
        float alphax = myY2 * fovx;
        float alphay = -myX2 * fovy;

        float[] handsRotationQuaternion = createQuaternionFromEulerAngles(alphax, alphay, cameraPose);

        return multiplyQuaternions(handsRotationQuaternion, cameraPose.getRotationQuaternion());
    }

    /**
     * When landmarks of a hand are available, it gets the camera's position, and gets the quaternion
     * as the orientation of the direction pointed by the finger. It uses this direction to get the
     * intersection point with a plan where a target triangle is. Check if the point is in the
     * triangle or not and returns the result through an IntersectionPoint.
     * <p>
     * TEST: given point D, vector orientation and plane equation
     * point D: (1, 2, -3)
     * orientation: (-2, 0, 3)
     * plane equation: 2x - y + 3z = 2
     *       Nx = 2.0f;
     *       Ny = -1.0f;
     *       Nz = 3.0f;
     * constants: {-Dpoint[0], -Dpoint[1], -Dpoint[2], 2}
     * result: E (-3.4, 2, 3.6)
     *
     * @param cameraPose: (Pose)
     * @param landmarkList: (List<LandmarkProto.NormalizedLandmark>)
     * @param coords: (float[9]) the triangle interface vertices.
     * @param point: (IntersectionPoint) to be filled with infos like the point corrdinates
     *             and whether the user is touching its finger and thumb or not.
     * @param fingerQuaternion: (float[4])
     */
    public static void checkTouchingIF(
            Pose cameraPose,
            List<LandmarkProto.NormalizedLandmark> landmarkList,
            float[] coords,
            IntersectionPoint point,
            float[] fingerQuaternion
    ) {
        if (landmarkList != null) {
            // D is
            float Dpoint[] = {
                    cameraPose.tx(),
                    cameraPose.ty(),
                    cameraPose.tz()
            };
            // TEST Dpoint
            /*float Dpoint[] = {
                     1,
                     2,
                    -3
            };*/

            float orientation[] = getDirectionVectorFromQuaternion(fingerQuaternion);

            // TEST orientation
            /*float orientation[] = {
                    -2,
                     0,
                     3
            };*/

            // Get plan equation created by the triangle ABC
            // Ax+By+Cz+D=0

            // Calculate vectors v1 and v2
            float v1x = coords[3] - coords[0];
            float v1y = coords[4] - coords[1];
            float v1z = coords[5] - coords[2];

            float v2x = coords[6] - coords[0];
            float v2y = coords[7] - coords[1];
            float v2z = coords[8] - coords[2];

            // Calculate the cross product to find the normal vector N
            float Nx = v1y * v2z - v1z * v2y;
            float Ny = v1z * v2x - v1x * v2z;
            float Nz = v1x * v2y - v1y * v2x;

            // Calculate the constant D
            float D = -(Nx * coords[0] + Ny * coords[1] + Nz * coords[2]);

            // TEST plane equation : 2x - y + 3z = 2
            /*Nx = 2.0f;
            Ny = -1.0f;
            Nz = 3.0f;*/

            // Get normal vector n of the plan: Nx, Ny, Nz
            // Verify n and orientation aren't orthogonal by checking the scalar product != 0
            // Calculate the dot product
            float dotProduct = Nx * orientation[0] + Ny * orientation[1] + Nz * orientation[2];

            // Check if the dot product is not equal to 0
            if (dotProduct == 0) {
                Log.i("checkTouchingIF method", "The dot product is equal to 0 : " +
                        dotProduct);
                return;
            }

            // Resolve the system with the parametric representation of the line (D, orientation)
            // and the plan equation to get E.
            // Define the coefficients of your linear equations
            /*
                coefficients = {
                  { X, Y, Z, T},
                };
             */
            double[][] coefficients = {
                    {-1, 0, 0, orientation[0]},
                    {0, -1, 0, orientation[1]},
                    {0, 0, -1, orientation[2]},
                    {Nx, Ny, Nz, 0}
            };
            // TEST cofficients
            /*double[][] coefficients = {
                    {-1, 0, 0, orientation[0]},
                    {0, -1, 0, orientation[1]},
                    {0, 0, -1, orientation[2]},
                    {Nx, Ny, Nz, 0}
            };*/

            // Define the constants on the right-hand side of the equations
            double[] constants = {-Dpoint[0], -Dpoint[1], -Dpoint[2], -D};
            // TEST constants
            //double[] constants = {-Dpoint[0], -Dpoint[1], -Dpoint[2], 2};

            // Create a RealMatrix from the coefficients
            RealMatrix coefficientsMatrix = new Array2DRowRealMatrix(coefficients);

            // Create a RealVector from the constants
            RealVector constantsVector = new ArrayRealVector(constants);

            // Create a solver using Singular Value Decomposition (SVD)
            DecompositionSolver solver = new SingularValueDecomposition(coefficientsMatrix).getSolver();

            // Solve the system of equations
            RealVector solution = solver.solve(constantsVector); // E point

            // Print the solution
            // TEST E should be (-3.4, 2, 3.6)
            //Log.i(TAG, orientation[0] + " " + orientation[1] + " " + orientation[2]);
            //Log.i(TAG, "Solution: " + solution);


            // Calculate the determinants of cross product EA^EB, EB^EC, EC^EA
            // if all determinants are >0 or <0, display "touching"

            float[] A = {
                    coords[0],
                    coords[1],
                    coords[2]
            };
            float[] B = {
                    coords[3],
                    coords[4],
                    coords[5]
            };
            float[] C = {
                    coords[6],
                    coords[7],
                    coords[8]
            };

            // Define the point E
            float[] myPointE = {
                    (float) solution.getEntry(0),
                    (float) solution.getEntry(1),
                    (float) solution.getEntry(2)
            };

            // Define the Intersection point object
            point.setPoint(myPointE);

            // Calculate vectors EA, EB, and EC
            float[] EA = vectorSubtraction(A, myPointE);
            float[] EB = vectorSubtraction(B, myPointE);
            float[] EC = vectorSubtraction(C, myPointE);

            // Calculate the cross product of EA with EB, EB with EC, and EC with EA
            float[] crossProductEAEB = crossProduct(EA, EB);
            float[] crossProductEBEC = crossProduct(EB, EC);
            float[] crossProductECEA = crossProduct(EC, EA);

            // Check if E is in the triangle
            if ((crossProductEAEB[0] > 0 && crossProductEBEC[0] > 0 && crossProductECEA[0] > 0) ||
                    (crossProductEAEB[0] < 0 && crossProductEBEC[0] < 0 && crossProductECEA[0] < 0) ||
                    (crossProductEAEB[1] > 0 && crossProductEBEC[1] > 0 && crossProductECEA[1] > 0) ||
                    (crossProductEAEB[1] < 0 && crossProductEBEC[1] < 0 && crossProductECEA[1] < 0) ||
                    (crossProductEAEB[2] > 0 && crossProductEBEC[2] > 0 && crossProductECEA[2] > 0) ||
                    (crossProductEAEB[2] < 0 && crossProductEBEC[2] < 0 && crossProductECEA[2] < 0)) {

                // Check the size between finger and thumb
                // get finger point
                float[] fingerPoint = new float[] {
                        landmarkList.get(HandLandmark.INDEX_FINGER_TIP).getX(),
                        landmarkList.get(HandLandmark.INDEX_FINGER_TIP).getY()
                };
                // get thumb point
                float[] thumbPoint = new float[] {
                        landmarkList.get(HandLandmark.THUMB_TIP).getX(),
                        landmarkList.get(HandLandmark.THUMB_TIP).getY()
                };
                // get the vector from finger point to thumb point
                float[] vec = new float[] {
                        thumbPoint[0] - fingerPoint[0],
                        thumbPoint[1] - fingerPoint[1]
                };
                // get its norm
                float norm = norm(vec);
                // check if the norm is less than 0.1
                if (norm < 0.1) {
                    point.setAction(true);
                } else {
                    point.setAction(false);
                }
            } else {
                point.setAction(false);
            }

        }
    }

    /**
     * Builds a perspective projection matrix.
     *
     * @param fovY The vertical field of view in radians.
     * @param aspect The aspect ratio of the viewport (width / height).
     * @param zNear The near clipping plane distance.
     * @param zFar The far clipping plane distance.
     * @param customM32 Custom value for the M[3][2] element in the matrix.
     * @return A 4x4 perspective projection matrix.
     */
    public static float[] buildPerspectiveMatrix(float fovX, float fovY, float aspect, float zNear, float zFar, float customM32) {
        // Convert FOVs from degrees to radians
        float fovXRad = (float) Math.toRadians(fovX);
        float fovYRad = (float) Math.toRadians(fovY);

        // Calculate tangent of half FOV angles
        float tanHalfFovX = (float) Math.tan(fovXRad / 2.0);
        float tanHalfFovY = (float) Math.tan(fovYRad / 2.0);

        // Create a float array for the matrix
        float[] projectionMatrix = new float[16];

        // Fill in the projection matrix values
        projectionMatrix[0]  = 1.0f / (aspect * tanHalfFovX);  // m00
        projectionMatrix[1]  = 0.0f;                          // m10
        projectionMatrix[2]  = 0.0f;                          // m20
        projectionMatrix[3]  = 0.0f;                          // m30

        projectionMatrix[4]  = 0.0f;                          // m01
        projectionMatrix[5]  = 1.0f / tanHalfFovY;            // m11
        projectionMatrix[6]  = 0.0f;                          // m21
        projectionMatrix[7]  = 0.0f;                          // m31

        projectionMatrix[8]  = 0.0f;                          // m02
        projectionMatrix[9]  = 0.0f;                          // m12
        projectionMatrix[10] = -(zFar + zNear) / (zFar - zNear); // m22
        projectionMatrix[11] = customM32;                     // m32

        projectionMatrix[12] = 0.0f;                          // m03
        projectionMatrix[13] = 0.0f;                          // m13
        projectionMatrix[14] = -(2.0f * zFar * zNear) / (zFar - zNear); // m23
        projectionMatrix[15] = 0.0f;                          // m33

        return projectionMatrix;
    }


    /**
     * Calculates the aspect ratio given the horizontal (fovx) and vertical (fovy) field of view angles.
     *
     * @param fovx The horizontal field of view in radians.
     * @param fovy The vertical field of view in radians.
     * @return The aspect ratio (width/height).
     */
    public static float calculateAspectRatio(float fovx, float fovy) {
        float tanFovxOver2 = (float) Math.tan(fovx / 2.0f);
        float tanFovyOver2 = (float) Math.tan(fovy / 2.0f);

        return tanFovxOver2 / tanFovyOver2;
    }

    /**
     * Calculates the viewport width given the horizontal field of view, vertical field of view, and viewport height.
     *
     * @param fovx The horizontal field of view in degrees.
     * @param fovy The vertical field of view in degrees.
     * @param viewportHeight The height of the viewport.
     * @return The width of the viewport.
     */
    public static float calculateViewportWidth(float fovx, float fovy, float viewportHeight) {
        // Convert FOVx and FOVy from degrees to radians
        float fovxRad = (float) Math.toRadians(fovx);
        float fovyRad = (float) Math.toRadians(fovy);

        // Calculate tangent of half of FOVx and FOVy
        float tanFovxOver2 = (float) Math.tan(fovxRad / 2.0);
        float tanFovyOver2 = (float) Math.tan(fovyRad / 2.0);

        // Calculate the aspect ratio
        float aspect = tanFovxOver2 / tanFovyOver2;

        // Calculate the viewport width
        return aspect * viewportHeight;
    }

    /**
     *
     * @param fovY
     * @param aspect
     * @param near
     * @param far
     * @return
     */
    public static float[] createPerspectiveMatrix(float fovY, float aspect, float near, float far) {
        float[] matrix = new float[16];

        float f = (float) (1.0 / Math.tan(fovY * 0.5));

        matrix[0] = f / aspect;
        matrix[1] = 0;
        matrix[2] = 0;
        matrix[3] = 0;

        matrix[4] = 0;
        matrix[5] = f;
        matrix[6] = 0;
        matrix[7] = 0;

        matrix[8] = 0;
        matrix[9] = 0;
        matrix[10] = (far + near) / (near - far);
        matrix[11] = -1;

        matrix[12] = 0;
        matrix[13] = 0;
        matrix[14] = (2 * far * near) / (near - far);
        matrix[15] = 0;

        return matrix;
    }
}
