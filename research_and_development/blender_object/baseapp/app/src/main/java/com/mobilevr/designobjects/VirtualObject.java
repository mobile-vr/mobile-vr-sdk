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

package com.mobilevr.designobjects;

import android.util.Log;

import com.mobilevr.modified.samplerender.Framebuffer;
import com.mobilevr.modified.samplerender.IndexBuffer;
import com.mobilevr.modified.samplerender.Mesh;
import com.mobilevr.modified.samplerender.SampleRender;
import com.mobilevr.modified.samplerender.Shader;
import com.mobilevr.modified.samplerender.Texture;
import com.mobilevr.modified.samplerender.VertexBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Objects;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;

/**
 * Class holding shaders and the parameters of a virtual object.
 */
public class VirtualObject {
    private static final String TAG = "mobilevr";
    private int VERTICES_PER_VERTEX;
    private float[] vertices;
    private int[] indexes;
    private FloatBuffer floatBuffer;
    private VertexBuffer vertexBuffer;
    private IntBuffer intBuffer;
    private IndexBuffer indexBuffer;
    public Mesh mesh;
    public Shader shader;
    public float[][] IFs;
    private String mode;
    private Map<String, SubObject> subObjects;

    /**
     * Creates a VirtualObject. Class holding shaders and
     * the parameters of a virtual object.
     *
     * @param render: SampleRender
     * @param myVERTICES_PER_VERTEX: (int) Usually you use 3 for the 3D position, but you can also
     *                             use 8 for the 3D position, the 3D RGB color, and the 2D
     *                             texture position. It depends on the shader you are using.
     * @param myVertices: (float[]) the vertices of the object.
     * @param myIndexes: (int[]) the indexes to organize the vertices.
     * @param vertexShaderFileName: (String) the vertex shader file name.
     * @param fragmentShaderFileName: (String) the fragment shader file name.
     * @param myIFs: (float[][]) one or multiple vertices of triangles should be given. They represent the 
     *                      surfaces to interact with this object.
     * @param myMode: (String) "useColor", "texture", "normal", this is the mode used by the Mesh
     *              object, it depends on the used shader.
     */
    public VirtualObject(
            SampleRender render,
            int myVERTICES_PER_VERTEX,
            float[] myVertices,
            int[] myIndexes,
            String vertexShaderFileName,
            String fragmentShaderFileName,
            float[][] myIFs,
            String myMode) {

        VERTICES_PER_VERTEX = myVERTICES_PER_VERTEX;
        vertices = myVertices;
        indexes = myIndexes;
        IFs = myIFs;
        mode = myMode;


        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                vertices.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());
        // create a floating point buffer from the ByteBuffer
        floatBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        floatBuffer.put(vertices);
        // set the buffer to read the first coordinate
        floatBuffer.position(0);
        vertexBuffer = new VertexBuffer(render, VERTICES_PER_VERTEX, floatBuffer);


        // initialize indexBuffer
        ByteBuffer bb2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                indexes.length * 4);
        // use the device hardware's native byte order
        bb2.order(ByteOrder.nativeOrder());
        // create a floating point buffer from the ByteBuffer
        intBuffer = bb2.asIntBuffer();
        // add the coordinates to the FloatBuffer
        intBuffer.put(indexes);
        // set the buffer to read the first coordinate
        intBuffer.position(0);
        indexBuffer = new IndexBuffer(render, intBuffer);

        mesh = new Mesh(
                render,
                Mesh.PrimitiveMode.TRIANGLES,
                indexBuffer,
                new VertexBuffer[]{vertexBuffer},
                mode);
        try {
            shader =
                    Shader.createFromAssets(
                            render,
                            vertexShaderFileName, // is for the position
                            fragmentShaderFileName, // fragment is for the color
                            null);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read a required asset file", e);
        }
    }

    public VirtualObject(SampleRender render,
                         Map<String, SubObject> mySubObjects,
                         String vertexShaderFileName,
                         String fragmentShaderFileName) {
        subObjects = mySubObjects;

        Log.i(TAG, "ici");

        for (Map.Entry<String, SubObject> entry : subObjects.entrySet()) {
            SubObject currentSubObject = Objects.requireNonNull(subObjects.get(entry.getKey()));
            Log.i(TAG, "ici : " + entry.getKey());
            try {
                Shader subObjectShader =
                        Shader.createFromAssets(
                                render,
                                vertexShaderFileName, // is for the position
                                fragmentShaderFileName, // fragment is for the color
                                null);

                // Get Texture paths
                String kdTexturePath = currentSubObject.getMtl().getMapKd();
                String ksTexturePath = currentSubObject.getMtl().getMapKs();
                String BumpTexturePath = currentSubObject.getMtl().getBump();

                // Get parameters
                FloatTuple kaFT = currentSubObject.getMtl().getKa();
                FloatTuple ksFT = currentSubObject.getMtl().getKs();
                FloatTuple keFT = currentSubObject.getMtl().getKe();
                FloatTuple textureOffset = currentSubObject.getMtl().getMapKdOptions().getO();
                Float ni = currentSubObject.getMtl().getNi();
                Float d = currentSubObject.getMtl().getD();
                Float bm = currentSubObject.getMtl().getBumpOptions().getBm();
                Integer illum = currentSubObject.getMtl().getIllum();

                if (kdTexturePath != null) {
                    Log.i(TAG, "kdTexturePath : " + kdTexturePath);
                    Texture texture = Texture.createFromAsset(
                            render,
                            kdTexturePath,
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.SRGB);

                    // set texture to shader
                    subObjectShader.setTexture("map_Kd", texture);
                }

                if (ksTexturePath != null) {
                    Log.i(TAG, "ksTexturePath : " + ksTexturePath);
                    Texture texture = Texture.createFromAsset(
                            render,
                            ksTexturePath,
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.SRGB);

                    // set texture to shader
                    subObjectShader.setTexture("map_Ks", texture);
                }

                if (BumpTexturePath != null) {
                    Log.i(TAG, "BumpTexturePath : " + BumpTexturePath);
                    Texture texture = Texture.createFromAsset(
                            render,
                            BumpTexturePath,
                            Texture.WrapMode.CLAMP_TO_EDGE,
                            Texture.ColorFormat.SRGB);

                    // set texture to shader
                    subObjectShader.setTexture("map_Bump", texture);
                }

                if (kaFT != null) {
                    // Set light parameter to shader
                    float[] ka = new float[] {kaFT.getX(), kaFT.getY(), kaFT.getZ()};
                    subObjectShader.setVec3("Ka", ka);
                }

                if (ksFT != null) {
                    // Set light parameter to shader
                    float[] ks = new float[] {ksFT.getX(), ksFT.getY(), ksFT.getZ()};

                    subObjectShader.setVec3("Ks", ks);
                }

                if (keFT != null) {
                    // Set light parameter to shader
                    float[] ke = new float[] {keFT.getX(), keFT.getY(), keFT.getZ()};

                    subObjectShader.setVec3("Ke", ke);
                }

                if (textureOffset != null) {
                    // Set light parameter to shader
                    float[] to = new float[] {textureOffset.getX(), textureOffset.getY(), textureOffset.getZ()};

                    subObjectShader.setVec3("textureOffset", to);
                }

                if (ni != null) {
                    subObjectShader.setFloat("Ni", ni);
                }

                if (d != null) {
                    subObjectShader.setFloat("d", d);
                }

                if (bm != null) {
                    subObjectShader.setFloat("bumpMultiplier", bm);
                }

                if (illum != null) {
                    subObjectShader.setInt("illum", illum);
                }

                // Set the final shader to the current SubObject instance
                currentSubObject.setShader(subObjectShader);

            } catch (IOException e) {
                Log.e(TAG, "Failed to read a required asset file", e);
            }
        }
    }

    public void draw(SampleRender render,
                     Framebuffer frameBuffer,
                     Map<String, Object> dynamicParameters,
                     float x0,
                     float y0,
                     float u,
                     float v) {
        for (Map.Entry<String, SubObject> entry : subObjects.entrySet()) {
            SubObject currentSubObject = Objects.requireNonNull(subObjects.get(entry.getKey()));
            currentSubObject.draw(render, frameBuffer, dynamicParameters, x0, y0, u, v);
        }
    }
}
