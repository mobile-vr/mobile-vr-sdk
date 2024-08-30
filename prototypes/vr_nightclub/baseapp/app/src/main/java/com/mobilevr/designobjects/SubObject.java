package com.mobilevr.designobjects;

import android.util.Log;

import com.mobilevr.modified.samplerender.Framebuffer;
import com.mobilevr.modified.samplerender.Mesh;
import com.mobilevr.modified.samplerender.SampleRender;
import com.mobilevr.modified.samplerender.Shader;

import java.util.Map;

import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;

public class SubObject {
    private static final String TAG = "mobilevr";
    private Mesh mesh;
    private Mtl mtl;
    private Shader shader;
    private Boolean enable=true;

    public SubObject() {

    }

    public void draw(SampleRender render,
                     Framebuffer frameBuffer,
                     Map<String, Object> dynamicParameters,
                     float x0,
                     float y0,
                     float u,
                     float v) {
        if (enable) {
            // Setting the position, scale and orientation to the square

            // Set the dynamic parameters
            for (Map.Entry<String, Object> entry : dynamicParameters.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof float[]) {
                    float[] floatArray = (float[]) value;
                    if (floatArray.length == 16) {
                        shader.setMat4(key, floatArray);
                    } else if (floatArray.length == 3) {
                        shader.setVec3(key, floatArray);
                    }
                }
            }

            // drawing the square on the virtual scene
            render.draw(mesh, shader, frameBuffer, 0, x0, y0, u, v);
            render.draw(mesh, shader, frameBuffer, 1, x0, y0, u, v);
        }
    }

    public void disable() {
        enable = false;
    }

    public void setMesh(Mesh myMesh) {
        mesh = myMesh;
    }

    public void setMtl(Mtl myMtl) {
        mtl = myMtl;
    }

    public void setShader(Shader myShader) {
        shader = myShader;
    }

    public Mtl getMtl() {
        return mtl;
    }
}
