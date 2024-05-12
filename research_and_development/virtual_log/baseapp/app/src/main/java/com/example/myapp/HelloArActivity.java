/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
 * Get the detailed modifications in "Licenses/log_git_diff/diff_HelloArActivity.diff"
 * at root of this app.
 */

package com.example.myapp;

import com.baseapp.R;

import static com.mobilevr.utils.GeometryUtils.getFingerQuaternion;
import static com.mobilevr.utils.QuaternionUtils.quaternionToMatrix;
import com.mobilevr.handstracking.HandsTrackingThread;
import com.mobilevr.designobjects.VirtualObject;
import com.mobilevr.log.BitmapData;
import com.mobilevr.modified.samplerender.Texture;
import com.mobilevr.modified.samplerender.arcore.BackgroundRenderer;
import com.mobilevr.modified.samplerender.Framebuffer;
import com.mobilevr.modified.samplerender.Mesh;
import com.mobilevr.modified.samplerender.SampleRender;
import com.mobilevr.modified.samplerender.Shader;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.DepthSettings;
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.ar.core.examples.java.common.helpers.FullScreenHelper;
import com.google.ar.core.examples.java.common.helpers.SnackbarHelper;
import com.google.ar.core.examples.java.common.helpers.TrackingStateHelper;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import com.google.mediapipe.formats.proto.LandmarkProto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;


public class HelloArActivity extends AppCompatActivity implements SampleRender.Renderer {
  static {
    System.loadLibrary("myTargetd");
  }
  private static final String TAG = "mobilevr";
  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 100f;

  // Rendering. The Renderers are created here, and initialized when the GL surface is created.
  private GLSurfaceView surfaceView;
  private boolean installRequested;
  private Session session;
  private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  private DisplayRotationHelper displayRotationHelper;
  private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(this);
  private SampleRender render;
  private BackgroundRenderer backgroundRenderer;
  private Framebuffer virtualSceneFramebuffer;
  private boolean hasSetTextureNames = false;
  private final DepthSettings depthSettings = new DepthSettings();

  // Line
  private VirtualObject lineObject;

  // Model, view, projection, VP and MVP Matrices.
  // FYI: Temporary matrix allocated here to reduce number of allocations for each frame.
  private final float[] modelMatrix = new float[16];
  private final float[] viewMatrix = new float[16];
  private final float[] projectionMatrix = new float[16];
  private final float[] uMVPMatrix = new float[16];
  private final float[] vPMatrix = new float[16];
  private float x0= (float) 0.1, y0= (float) 0.1, u= (float) 0.8, v= (float) 0.4;

  // Debug
  private long time;
  public Context nonUiContext;
  private Boolean drawPointer, fixCamera;
  private float[] cameraPosition = new float[3];

  // Hands tracking
  private HandsTrackingThread handsTrackingThread;
  private float fovx, fovy;
  private float[] fingerQuaternion;

  // ======================================================================================= //
  //                                        keep above
  // ======================================================================================= //

  // Implement your variables here

  private VirtualObject squareObject;

  // ======================================================================================= //
  //                                        keep below
  // ======================================================================================= //

  /**
   * Creates the android app.
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    surfaceView = findViewById(R.id.surfaceview);

    // Helper to measure the orientation of the device
    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

    // Set up renderer.
    render = new SampleRender(surfaceView, this, getAssets());

    installRequested = false;
    nonUiContext = getApplicationContext();
    depthSettings.onCreate(this);

    // horizontal and vertical fov
    // Using the phone in portrait, the y is vertical and x is horizontal.
    fovx = 40.0f * (float) (Math.PI / 180);
    fovy = 50.0f * (float) (Math.PI / 180);

    // DEBUG PARAMETERS
    drawPointer = true;
    fixCamera = true;
    if (fixCamera) {
      cameraPosition = new float[] {0, 0, 0};
    }
  }

  /**
   * When the app's main thread gets killed.
   */
  @Override
  protected void onDestroy() {
    if (session != null) {
      // Explicitly close ARCore Session to release native resources.
      // Review the API reference for important considerations before calling close() in apps with
      // more complicated lifecycle requirements:
      // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
      session.close();
      session = null;
    }

    // Interrupt HandsTrackingThread
    if (handsTrackingThread != null) {
      handsTrackingThread.interrupt();
    }

    super.onDestroy();
  }

  /**
   * Called when the app is launched, or when the user has opened another app without killing
   * the main (ui) thread, and gets back on it.
   * <p>
   * Also, it creates and starts a new hands tracking thread.
   */
    @Override
    protected void onResume () {
      super.onResume();

      if (session == null) {
        Exception exception = null;
        String message = null;
        try {
          switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
            case INSTALL_REQUESTED:
              installRequested = true;
              return;
            case INSTALLED:
              break;
          }

          // ARCore requires camera permissions to operate. If we did not yet obtain runtime
          // permission on Android M and above, now is a good time to ask the user for it.
          if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
          }

          // Create the session.
          session = new Session(/* context= */ this);
        } catch (UnavailableArcoreNotInstalledException
                 | UnavailableUserDeclinedInstallationException e) {
          message = "Please install ARCore";
          exception = e;
        } catch (UnavailableApkTooOldException e) {
          message = "Please update ARCore";
          exception = e;
        } catch (UnavailableSdkTooOldException e) {
          message = "Please update this app";
          exception = e;
        } catch (UnavailableDeviceNotCompatibleException e) {
          message = "This device does not support AR";
          exception = e;
        } catch (Exception e) {
          message = "Failed to create AR session";
          exception = e;
        }

        if (message != null) {
          messageSnackbarHelper.showError(this, message);
          Log.e(TAG, "Exception creating session", exception);
          return;
        }
      }

      // Create and start new HandsTrackingThread
      Thread asyncThread = new Thread(new Runnable() {
        @Override
        public void run() {
          // Create and start new HandsTrackingThread
          handsTrackingThread = new HandsTrackingThread(nonUiContext);
          handsTrackingThread.start();
          Log.i(
                  TAG,
                  "Starting hands tracking thread: " +
                          Boolean.toString(handsTrackingThread.isRunning)
          );
        }
      });
      asyncThread.start();

      // Note that order matters - see the note in onPause(), the reverse applies here.
      try {
        configureSession();
        // To record a live camera session for later playback, call
        // `session.startRecording(recordingConfig)` at anytime. To playback a previously recorded AR
        // session instead of using the live camera feed, call
        // `session.setPlaybackDatasetUri(Uri)` before calling `session.resume()`. To
        // learn more about recording and playback, see:
        // https://developers.google.com/ar/develop/java/recording-and-playback
        session.resume();
      } catch (CameraNotAvailableException e) {
        messageSnackbarHelper.showError(
                this,
                "Camera not available. Try restarting the app."
        );
        session = null;
        return;
      }

      surfaceView.onResume();
      displayRotationHelper.onResume();
    }

    /**
     * When the user goes on another app.
     */
    @Override
    public void onPause () {
      super.onPause();

      if (session != null) {
        // Note that the order matters - GLSurfaceView is paused first so that it does not try
        // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
        // still call session.update() and get a SessionPausedException.
        displayRotationHelper.onPause();
        surfaceView.onPause();
        session.pause();
      }

      // Interrupt handsTrackingThread
      if (handsTrackingThread != null) {
        handsTrackingThread.interrupt();
      }
    }

    /**
     * Check if the app has camera permissions.
     *
     * @param requestCode
     * @param permissions
     * @param results
     */
    @Override
    public void onRequestPermissionsResult ( int requestCode, String[] permissions,int[] results){
      super.onRequestPermissionsResult(requestCode, permissions, results);
      if (!CameraPermissionHelper.hasCameraPermission(this)) {
        // Use toast instead of snackbar here since the activity will exit.
        Toast.makeText(
                        this,
                        "Camera permission is needed to run this application",
                        Toast.LENGTH_LONG)
                .show();
        if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
          // Permission denied with checking "Do not ask again".
          CameraPermissionHelper.launchPermissionSettings(this);
        }
        finish();
      }
    }

    /**
     * I don't know.
     * Probably setting full screen.
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged ( boolean hasFocus){
      super.onWindowFocusChanged(hasFocus);
      FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    /**
     * The best place to create all your objects.
     * <p>
     * Objects:
     *    - Line: the pointer
     *    - Cube
     *
     * @param render : the object drawing on the phone's screen.
     */
    @Override
    public void onSurfaceCreated (SampleRender render){
      // To render the image taken from the camera at the background
      backgroundRenderer = new BackgroundRenderer(render);
      virtualSceneFramebuffer = new Framebuffer(render, /*width=*/ 1, /*height=*/ 1);

      // LINE object init: this is the pointer
      float[] lineCoords = {
              // Front face
              -0.005f, -0.005f, 10.0f,
              0.005f, -0.005f, 10.0f,
              0.005f, 0.005f, 10.0f,
              -0.005f, 0.005f, 10.0f,

              // Back face
              -0.005f, -0.005f, -10.0f,
              0.005f, -0.005f, -10.0f,
              0.005f, 0.005f, -10.0f,
              -0.005f, 0.005f, -10.0f
      };
      int[] lineIndex = {
              // Front face
              0, 1, 2,
              0, 2, 3,

              // Back face
              4, 6, 5,
              4, 7, 6,

              // Left face
              4, 5, 1,
              4, 1, 0,

              // Right face
              3, 2, 6,
              3, 6, 7,

              // Top face
              1, 5, 6,
              1, 6, 2,

              // Bottom face
              4, 0, 3,
              4, 3, 7
      };
      int COORDS_PER_VERTEX = 3;
      String vertexShaderFileName = "shaders/simpleShader.vert";
      String fragmentShaderFileName = "shaders/simpleShader.frag";
      String mode = "simple";
      lineObject = new VirtualObject(
              render,
              COORDS_PER_VERTEX,
              lineCoords,
              lineIndex,
              vertexShaderFileName,
              fragmentShaderFileName,
              null,
              mode);

      // ======================================================================================= //
      //                                        keep above
      // ======================================================================================= //

      // Implement the objects of your game here.

      try {
        // Open an InputStream to the font file in assets
        AssetManager assetManager = getAssets();
        InputStream inputStream = assetManager.open("fonts/16020_FUTURAM.ttf");

        // Read the font file into a byte array
        byte[] fontData = new byte[inputStream.available()]; // deallocate after calling FT_Done_Face
        inputStream.read(fontData);
        inputStream.close();

        // Pass the byte array to the native method
        long fontPtr = loadFontFromAssets(fontData);
        Log.i(TAG, String.valueOf(fontPtr));

        int numGlyphs = get_num_glyphs(fontPtr);
        Log.i(TAG, "numGlyphs: " + numGlyphs);

        // For each wanted character create a texture in OpenGL

        BitmapData characterBitmap = getCharacterBitmap(fontPtr, 0x0065);
        Log.i(TAG, String.valueOf(characterBitmap.getHeight()));

        // Create Bitmap from pixel data
        Bitmap bitmap = Bitmap.createBitmap(characterBitmap.getWidth(),
                characterBitmap.getHeight(), Bitmap.Config.ALPHA_8);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(characterBitmap.getData()));

        // square Texture init
        Texture squareTexture = Texture.createFromBitmap(
                render,
                bitmap,
                Texture.WrapMode.CLAMP_TO_EDGE);

        // Create a square
        /*float[] squareCoords = { // counterclock order
                // Front face
                -0.4f, -0.2f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                -0.4f, 0.4f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.4f, 0.4f, -2.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.4f, -0.2f, -2.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f
        };*/
        float[] squareCoords = { // counterclock order
                // Front face
                -0.2f, -0.2f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                -0.2f, 0.2f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.2f, 0.2f, -2.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.2f, -0.2f, -2.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f
        };
        int[] squareIndex = {
                // Front face
                0, 1, 2,
                0, 2, 3
        };
        COORDS_PER_VERTEX = 8; // 3 for position, 3 for color, 2 for texture coordinates
        vertexShaderFileName = "shaders/textureShader.vert";
        fragmentShaderFileName = "shaders/textureShader.frag";
        mode = "texture";
        squareObject = new VirtualObject(
                render,
                COORDS_PER_VERTEX,
                squareCoords,
                squareIndex,
                vertexShaderFileName,
                fragmentShaderFileName,
                null,
                mode);

        // apply the texture to the square
        squareObject.shader.setTexture("ourTexture", squareTexture);

        // Use the fontPtr as needed
      } catch (IOException e) {
        e.printStackTrace();
        Log.e(TAG, e.toString());
      }

      // ======================================================================================= //
      //                                        keep below
      // ======================================================================================= //
    }

    /**
     * Resize the image displayed.
     *
     * @param render
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged (SampleRender render,int width, int height){
      int myHeight = (int) Math.floor(height / 2.0);
      displayRotationHelper.onSurfaceChanged(width, myHeight);
      // why only the virtual and not also the window scene?
      virtualSceneFramebuffer.resize(width, height);
    }

    /**
     * Draws the objects defined in onSurfaceCreated() on the 2D screen.
     *
     * @param render: (SampleRender)
     */
    @Override
    public void onDrawFrame (SampleRender render){
      if (session == null) {
        return;
      }

      // Texture names should only be set once on a GL thread unless they change. This is done during
      // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
      // initialized during the execution of onSurfaceCreated.
      if (!hasSetTextureNames) {
        session.setCameraTextureNames(
                new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
        hasSetTextureNames = true;
      }

      // -- Update per-frame state

      // Notify ARCore session that the view size changed so that the perspective matrix and
      // the video background can be properly adjusted.
      displayRotationHelper.updateSessionIfNeeded(session);

      // Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      Frame frame;
      try {
        frame = session.update();
      } catch (CameraNotAvailableException e) {
        Log.e(TAG, "Camera not available during onDrawFrame", e);
        messageSnackbarHelper.showError(
                this,
                "Camera not available. Try restarting the app."
        );
        return;
      }
      Camera camera = frame.getCamera();
      // Get the cameraPose
      // Note: in versions < 1.5: physical camera: opens in landscape (90° from portrait
      //       according to -Z axis)
      Pose cameraPose = camera.getPose();
      if (fixCamera) {
        cameraPose = new Pose(cameraPosition, camera.getPose().getRotationQuaternion());
      } else {
        cameraPosition = new float[]{
                cameraPose.tx(),
                cameraPose.ty(),
                cameraPose.tz()
        };
      }

      // Update BackgroundRenderer state to match the depth settings.
      try {
        backgroundRenderer.setUseDepthVisualization(
                render, depthSettings.depthColorVisualizationEnabled());
        backgroundRenderer.setUseOcclusion(render, depthSettings.useDepthForOcclusion());
      } catch (IOException e) {
        Log.e(TAG, "Failed to read a required asset file", e);
        messageSnackbarHelper.showError(
                this,
                "Failed to read a required asset file: " + e
        );
        return;
      }
      // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
      // used to draw the background camera image.
      backgroundRenderer.updateDisplayGeometry(frame);

      if (camera.getTrackingState() == TrackingState.TRACKING
              && (depthSettings.useDepthForOcclusion()
              || depthSettings.depthColorVisualizationEnabled())) { // Never true?
        try (Image depthImage = frame.acquireCameraImage()) {
          backgroundRenderer.updateCameraDepthTexture(depthImage);

        } catch (NotYetAvailableException e) {
          // This normally means that depth data is not available yet. This is normal so we will not
          // spam the logcat with this.
        }
      }

      // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
      trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

      // -- Draw background
      if (frame.getTimestamp() != 0) {
        // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
        // drawing possible leftover data from previous sessions if the texture is reused.
        backgroundRenderer.drawBackground(render, x0, y0, u, v); // THIS DRAWS THE IMAGE ON
                                                                 // THE BACKGROUND
      }

      // If not tracking, don't draw 3D objects.
      if (camera.getTrackingState() == TrackingState.PAUSED) {
        return;
      }

      // Get projection matrix.
      camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

      // Get camera matrix and draw.
    /*
        | 1 0 0 -eye_x |
        | 0 1 0 -eye_y |
        | 0 0 1 -eye_z |
        | 0 0 0    1   |
     */
      camera.getViewMatrix(viewMatrix, 0);

      // Fix camera - necessary since the Android 13 from my phone breaks the sensors calibration
      if (fixCamera) {
        viewMatrix[12] = -cameraPosition[0];
        viewMatrix[13] = -cameraPosition[1];
        viewMatrix[14] = -cameraPosition[2];
      }

      // create the Projection-View matrix
      Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

      // Clear the screen before drawing the next frame
      render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f, 2, x0, y0, u, v);


      // Get landmarkList
      List<LandmarkProto.NormalizedLandmark> landmarkList = null;
      if (handsTrackingThread != null) {
        landmarkList = handsTrackingThread.getLandmarkList();
      }


      // Get fingerQuaternion
      if (landmarkList != null) {
        fingerQuaternion = getFingerQuaternion(cameraPose, landmarkList, fovx, fovy);
      } else {
        fingerQuaternion = null;
      }

      // ========================================================================================= //
      //                                        keep above
      // ========================================================================================= //

      // Implement the drawing behavior of your game here.

      // DRAW SQUARE
      // applying transformations:
      Matrix.setIdentityM(modelMatrix, 0);
      //Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, 0.0f);
      //Matrix.scaleM(modelMatrix, 0, 1.5f, 1.5f, 1.5f);
      //Matrix.rotateM(modelMatrix, 0, -45f, 0, 0, -1.0f);
      Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

      // Setting the position, scale and orientation to the square
      squareObject.shader.setMat4("uMVPMatrix", uMVPMatrix);

      // drawing the square
      render.draw(squareObject.mesh, squareObject.shader, virtualSceneFramebuffer, 0, x0, y0, u, v);
      render.draw(squareObject.mesh, squareObject.shader, virtualSceneFramebuffer, 1, x0, y0, u, v);









      // ========================================================================================= //
      //                                        keep below
      // ========================================================================================= //

      // If the pointer is wanted to be drawn
      if (drawPointer && fingerQuaternion != null) {

        float[] pointerPosition;

        if (fixCamera) {
          pointerPosition = new float[]{0, 0, 0};
        } else {
          pointerPosition = new float[]{
                  cameraPose.tx(),
                  cameraPose.ty(),
                  cameraPose.tz()
          };
        }

        // DRAW Line which is the pointer

        // applying transformations:
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, pointerPosition[0],
                pointerPosition[1],
                pointerPosition[2]);
        Matrix.scaleM(modelMatrix, 0, 0.1f, 0.1f, 0.1f);

        // Convert the quaternion to a rotation matrix
        float[] rotationMatrix = quaternionToMatrix(fingerQuaternion);

        // Apply the rotation matrix to your model matrix
        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationMatrix, 0);
        Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

        // setting the color
        lineObject.shader.setVec4("vColor", new float[]{0.63671875f, 0.76953125f, 0.22265625f, 1.0f});

        // Setting the position, scale and orientation to the square
        lineObject.shader.setMat4("uMVPMatrix", uMVPMatrix);

        // drawing the line
        render.draw(lineObject.mesh, lineObject.shader, virtualSceneFramebuffer, 0, x0, y0, u, v);
        render.draw(lineObject.mesh, lineObject.shader, virtualSceneFramebuffer, 1, x0, y0, u, v);
      }

      // Compose the virtual scene with the background. i.e. Draw the virtual scene
      backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR, x0, y0, u, v);

      // Set a new image every 0ms to handsTrackingThread
      if (System.currentTimeMillis() - time > 0) {

        // Set camera image for the other thread
        try (Image cameraImage = frame.acquireCameraImage()) {
          if (handsTrackingThread != null) {
            handsTrackingThread.setCameraImage(cameraImage);
          }
        } catch (NotYetAvailableException e) {
          // This normally means that depth data is not available yet. This is normal so we will not
          // spam the logcat with this.
        }
        time = System.currentTimeMillis();
      }
    }

    /**
     * Configures the session with feature settings.
     */
    private void configureSession () {
      Config config = session.getConfig();
      config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
      if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
        config.setDepthMode(Config.DepthMode.AUTOMATIC);
      } else {
        config.setDepthMode(Config.DepthMode.DISABLED);
      }
      List<CameraConfig> cameraConfigList = session.
              getSupportedCameraConfigs(new CameraConfigFilter(session));
      for (int i = 0; i < cameraConfigList.size(); i++) {
        //Log.i(TAG, "i = " + i + ": " + cameraConfigList.get(i).getFpsRange().toString());
      }
      //session.setCameraConfig(cameraConfig);
      session.configure(config);
    }

    public native String stringFromJNI();
    public native long loadFont(String fontPath);
    public native long loadFontFromAssets(byte[] fontData);
    public native int get_num_glyphs(long face);
    public native BitmapData getCharacterBitmap(long face, long charCode);
}

