package com.mobilevr.sound;

import static com.mobilevr.utils.QuaternionUtils.quaternionToMatrix;
import android.opengl.Matrix;
import com.mobilevr.utils.VectorUtils;

public class SoundUtils {
    /**
     * Returns the azimut of the listener
     *
     * @param soundPos
     * @param listenerPos
     * @param camQuat
     * @return float: azimut
     */
    public static float processOrientation(float[] soundPos, float[] listenerPos, float[] camQuat) {
        float azimut;

        // Calculate the relative position
        float[] relPos = new float[] {
                - soundPos[0] + listenerPos[0],
                soundPos[1] - listenerPos[1],
                soundPos[2] - listenerPos[2],
                1
        };

        // Extract rotation around Y axis from camera quaternion
        float[] camRotationMatrix = quaternionToMatrix(camQuat);
        float[] yRotationMatrix = new float[] {
                camRotationMatrix[0], 0, camRotationMatrix[2], 0,
                0, 1, 0, 0,
                camRotationMatrix[8], 0, camRotationMatrix[10], 0,
                0, 0, 0, 1
        };

        // Apply Y rotation to the relPos vector
        float[] relPosRotated = new float[4];
        Matrix.multiplyMV(relPosRotated, 0, yRotationMatrix, 0, relPos, 0);

        azimut = (float) Math.toDegrees( Math.atan2(relPosRotated[0], relPosRotated[3]) );

        azimut = ( azimut + 360 ) % 360;

        // Reverse rotation
        azimut = 360 - azimut;

        return azimut;
    }

    /**
     * Returns the input volume from 0 to 1.
     *
     * @param soundPos
     * @param listenerPos
     * @param maxHearingDistance
     * @return
     */
    public static float processInputVolume(float[] soundPos,
                                     float[] listenerPos,
                                     float maxHearingDistance) {
        float inputVolume;

        // Calculate the relative position
        float[] relPos = new float[] {
                soundPos[0] - listenerPos[0],
                soundPos[1] - listenerPos[1],
                soundPos[2] - listenerPos[2]
        };
        float distance = VectorUtils.norm(relPos);

        // ratio distance from sound to listener / max hearing distance
        inputVolume = - distance / maxHearingDistance + 1;
        if (distance >= maxHearingDistance) {
            return 0;
        } else {
            return inputVolume;
        }
    }
}
