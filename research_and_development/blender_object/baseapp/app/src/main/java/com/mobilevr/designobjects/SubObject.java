package com.mobilevr.designobjects;

import com.mobilevr.modified.samplerender.Framebuffer;
import com.mobilevr.modified.samplerender.Mesh;
import com.mobilevr.modified.samplerender.SampleRender;
import com.mobilevr.modified.samplerender.Shader;

import de.javagl.obj.Mtl;

public class SubObject {
    private Mesh mesh;
    private Mtl mtl;
    private Shader shader;
    private Boolean enable=true;

    public SubObject() {

    }

    public void draw(SampleRender render,
                     Framebuffer frameBuffer,
                     float[] uMVPMatrix,
                     float x0,
                     float y0,
                     float u,
                     float v) {
        // Setting the position, scale and orientation to the square
        shader.setMat4("uMVPMatrix", uMVPMatrix);
        // drawing the square on the virtual scene
        render.draw(mesh, shader, frameBuffer, 0, x0, y0, u, v);
        render.draw(mesh, shader, frameBuffer, 1, x0, y0, u, v);
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
