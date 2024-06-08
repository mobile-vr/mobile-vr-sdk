/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2024 MasterHansCoding (GitHub)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications:
 *
 * Get the detailed modifications in "Licenses/log_git_diff/diff_SampleRender.diff"
 * at root of this app.
 */

package com.mobilevr.modified.samplerender;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** A SampleRender context. */
public class SampleRender {
  private static final String TAG = SampleRender.class.getSimpleName();

  private final AssetManager assetManager;

  private int viewportWidth = 1;
  private int viewportHeight = 1;

  /**
   * Constructs a SampleRender object and instantiates GLSurfaceView parameters.
   *
   * @param glSurfaceView Android GLSurfaceView
   * @param renderer Renderer implementation to receive callbacks
   * @param assetManager AssetManager for loading Android resources
   */
  public SampleRender(GLSurfaceView glSurfaceView, Renderer renderer, AssetManager assetManager) {
    this.assetManager = assetManager;
    glSurfaceView.setPreserveEGLContextOnPause(true);
    glSurfaceView.setEGLContextClientVersion(3);
    glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
    glSurfaceView.setRenderer(
        new GLSurfaceView.Renderer() {
          @Override
          public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES30.glEnable(GLES30.GL_BLEND);
            GLError.maybeThrowGLException("Failed to enable blending", "glEnable");
            renderer.onSurfaceCreated(SampleRender.this);
          }

          @Override
          public void onSurfaceChanged(GL10 gl, int w, int h) {
            viewportWidth = w;
            viewportHeight = h;
            renderer.onSurfaceChanged(SampleRender.this, w, h);
          }

          @Override
          public void onDrawFrame(GL10 gl) {
            clear(/*framebuffer=*/ null, 0f, 0f, 0f, 1f, 0,
                    0, (float) 0.08, 1, (float) 0.42);
            clear(/*framebuffer=*/ null, 0f, 0f, 0f, 1f, 1,
                    0, (float) 0.08, 1, (float) 0.42);
            renderer.onDrawFrame(SampleRender.this);
          }
        });
    glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    glSurfaceView.setWillNotDraw(false);
  }

  /** Draw a {@link Mesh} with the specified {@link Shader}. */
  public void draw(Mesh mesh, Shader shader, int side, float x0, float y0,
                   float u, float v) {
    draw(mesh, shader, /*framebuffer=*/ null, side, x0, y0, u, v);
  }

  /**
   * Draw a {@link Mesh} with the specified {@link Shader} to the given {@link Framebuffer}.
   *
   * <p>The {@code framebuffer} argument may be null, in which case the default framebuffer is used.
   */
  public void draw(Mesh mesh, Shader shader, Framebuffer framebuffer, int side, float x0, float y0,
                   float u, float v) {
    useFramebuffer(framebuffer, side, x0, y0, u, v);
    shader.lowLevelUse();
    mesh.lowLevelDraw();
  }

  /**
   * Clear the given framebuffer.
   *
   * <p>The {@code framebuffer} argument may be null, in which case the default framebuffer is
   * cleared.
   */
  public void clear(Framebuffer framebuffer, float r, float g, float b, float a, int side, float x0, float y0,
                    float u, float v) {
    useFramebuffer(framebuffer, side, x0, y0, u, v);
    GLES30.glClearColor(r, g, b, a);
    GLError.maybeThrowGLException("Failed to set clear color", "glClearColor");
    GLES30.glDepthMask(true);
    GLError.maybeThrowGLException("Failed to set depth write mask", "glDepthMask");
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
    GLError.maybeThrowGLException("Failed to clear framebuffer", "glClear");
  }

  /** Interface to be implemented for rendering callbacks. */
  public static interface Renderer {
    /**
     * Called by {@link SampleRender} when the GL render surface is created.
     *
     * <p>See {@link GLSurfaceView.Renderer#onSurfaceCreated}.
     */
    public void onSurfaceCreated(SampleRender render);

    /**
     * Called by {@link SampleRender} when the GL render surface dimensions are changed.
     *
     * <p>See {@link GLSurfaceView.Renderer#onSurfaceChanged}.
     */
    public void onSurfaceChanged(SampleRender render, int width, int height);

    /**
     * Called by {@link SampleRender} when a GL frame is to be rendered.
     *
     * <p>See {@link GLSurfaceView.Renderer#onDrawFrame}.
     */
    public void onDrawFrame(SampleRender render);
  }

  /* package-private */
  AssetManager getAssets() {
    return assetManager;
  }

  /**
   * Define a frame where to draw.
   * When the viewport is defined. We use the point (x0, y0) as the bottom left corner
   * of the frame in landscape view, and (u, v) a vector.
   * <p>
   * x0, y0, u, v are factors to position the viewport and adapt it to the user's cardboard.
   * It is a percentage (from 0 to 1).
   * y0 + v = 0.5 to center the line between the 2 views at the screen's center in landscape view.
   * x0 + x0 + u = 1 to have the same space from the frame and the screen sides at the bottom
   * and the top, in landscape view.
   * <p>
   * For example:
   *    x0 = 0, y0 = 0.08
   *    u = 1, v = 0.42
   * Example 2:
   *    x0 = 0.1, y0 = 0.1
   *    u = 0.8, v = 0.4
   * Exemple 3:
   *    x0 = 0.15, y0 = 0.1
   *    u = 0.7, v = 0.4
   *
   * @param framebuffer: FrameBuffer.
   * @param side: int, 0 for left, 1 for right.
   * @param x0: float, x starting point
   * @param y0: float, y starting point
   * @param u: float, x vector component
   * @param v: float, y vector component
   */
  private void useFramebuffer(Framebuffer framebuffer, int side, float x0, float y0, float u, float v) {
    int framebufferId;
    int viewportWidth;
    int viewportHeight;
    if (framebuffer == null) {
      framebufferId = 0;
      viewportWidth = this.viewportWidth;
      viewportHeight = this.viewportHeight;

      GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
      GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");

      // BOTTOM
      if (side == 0) {
        GLES30.glViewport(
                (int) Math.round(viewportWidth * x0),
                (int) Math.round(viewportHeight * y0),
                (int) Math.round(viewportWidth * u),
                (int) Math.round(viewportHeight * v));

      // TOP
      } else if (side == 1) {
        GLES30.glViewport(
                (int) Math.round(viewportWidth * x0),
                (int) Math.round(viewportHeight * 0.5),
                (int) Math.round(viewportWidth * u),
                (int) Math.round(viewportHeight * v));

      // ALL SCREEN
      } else if (side == 2) {
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
      }

      GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");

    } else {
      framebufferId = framebuffer.getFramebufferId();
      viewportWidth = framebuffer.getWidth();
      viewportHeight = framebuffer.getHeight();

      // BOTTOM
      if (side == 0) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");

        GLES30.glViewport(
                (int) Math.round(viewportWidth * x0),
                (int) Math.round(viewportHeight * y0),
                (int) Math.round(viewportWidth * u),
                (int) Math.round(viewportHeight * v));
        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");

      // TOP
      } else if (side == 1) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");

        GLES30.glViewport(
                (int) Math.round(viewportWidth * x0),
                (int) Math.round(viewportHeight * 0.5),
                (int) Math.round(viewportWidth * u),
                (int) Math.round(viewportHeight * v));

        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");

      // ALL SCREEN FOR CLEAR
      } else if (side == 2) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId);
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer");
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight);
        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport");
      }
    }
  }
}
