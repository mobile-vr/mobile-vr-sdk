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
}
