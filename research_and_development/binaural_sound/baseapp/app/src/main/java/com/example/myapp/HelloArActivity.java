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

import com.mobilevr.modified.samplerender.arcore.BackgroundRenderer;
import com.mobilevr.modified.samplerender.Framebuffer;
import com.mobilevr.modified.samplerender.Mesh;
import com.mobilevr.modified.samplerender.SampleRender;
import com.mobilevr.modified.samplerender.Shader;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.Image;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
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
import com.mobilevr.utils.GeometryUtils;
import com.mobilevr.utils.VectorUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class HelloArActivity extends AppCompatActivity implements SampleRender.Renderer {
  static {
    System.loadLibrary("PlayerExample");
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
  private Boolean fixCamera;
  private float[] cameraPosition = new float[3];

  // ======================================================================================= //
  //                                        keep above
  // ======================================================================================= //

  // Implement your variables here

  // Cube
  private Mesh cubeObjectMesh;
  private Shader cubeObjectShader;
  private double timer1, timer2;
  private int counter1=0, counter2=0;

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

    // DEBUG PARAMETERS
    fixCamera = false;
    if (fixCamera) {
      cameraPosition = new float[] {0, 0, 0};
    }

    // ======================================================================================= //
    //                                        keep above
    // ======================================================================================= //

    // Get Sample rate and buffer size
    String samplerateString = null, buffersizeString = null;

    AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

    if (audioManager != null) {
      samplerateString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
      buffersizeString = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
    }

    if (samplerateString == null) samplerateString = "48000";

    if (buffersizeString == null) buffersizeString = "480";

    int samplerate = Integer.parseInt(samplerateString);

    int buffersize = Integer.parseInt(buffersizeString);


    // Get the offset and length to know where our file is located.
    AssetFileDescriptor fd = getResources().openRawResourceFd(R.raw.track);
    int fileOffset = (int)fd.getStartOffset();
    int fileLength = (int)fd.getLength();
    try {
      fd.getParcelFileDescriptor().close();
    } catch (IOException e) {
      Log.e("PlayerExample", "Close error.");
    }

    // get path to APK package
    String path = getPackageResourcePath();

    // start audio engine
    NativeInit(samplerate, buffersize, getCacheDir().getAbsolutePath());

    // open audio file from APK
    OpenFileFromAPK(path, fileOffset, fileLength);

    // ======================================================================================= //
    //                                        keep below
    // ======================================================================================= //
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

    Cleanup();

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

      onForeground();
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

      onBackground();
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

      // ======================================================================================= //
      //                                        keep above
      // ======================================================================================= //

      // Implement the objects of your game here.

      // Prepare the rendering objects. This involves reading shaders and 3D model files, so may throw
      // an IOException.
      try {
        // For example here's a CUBE
        cubeObjectMesh = Mesh.createFromAsset(render, "models/cube.obj");

        cubeObjectShader =
                Shader.createFromAssets(
                        render,
                        "shaders/simpleShader.vert", // .vert is for the position
                        "shaders/simpleShader.frag", // .frag is for the color
                        null);

      } catch (IOException e) {
        Log.e(TAG, "Failed to read a required asset file", e);
      }

      // TogglePlayback
      TogglePlayback();

      timer1 = System.currentTimeMillis();
      timer2 = System.currentTimeMillis();

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

      // ========================================================================================= //
      //                                        keep above
      // ========================================================================================= //

      // Implement the drawing behavior of your game here.

      // For example here's a CUBE
      // applying transformations:
      Matrix.setIdentityM(modelMatrix, 0);
      Matrix.translateM(modelMatrix, 0, 0.0f, 0f, -1.0f);
      Matrix.scaleM(modelMatrix, 0, 0.1f, 0.1f, 0.1f);
      //Matrix.rotateM(modelMatrix, 0, -45f, 0, 0, -1.0f);
      Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);
      // setting the color
      cubeObjectShader.setVec4("vColor", new float[]{0.63671875f, 0.76953125f, 0.22265625f, 1.0f});
      // Setting the position, scale and orientation to the square
      cubeObjectShader.setMat4("uMVPMatrix", uMVPMatrix);
      // drawing the square on the virtual scene
      render.draw(cubeObjectMesh, cubeObjectShader, virtualSceneFramebuffer, 0, x0, y0, u, v);
      render.draw(cubeObjectMesh, cubeObjectShader, virtualSceneFramebuffer, 1, x0, y0, u, v);


      // Spatializer
      float[] soundPos = new float[]{0, 0, -1.0f};
      float[] camQuat = camera.getPose().getRotationQuaternion();
      Log.i(TAG, "camQuat: " + Arrays.toString(camQuat));
      float maxHearingDistance = 1.5f;

      float[] res = processOrientation(soundPos, cameraPosition, camQuat);
      float inputVolume = processInputVolume(soundPos, cameraPosition, maxHearingDistance);

      // Test
      /*float[] res = new float[0];
      // if timer < 100s
      if (System.currentTimeMillis() - timer1 < 1000000) {
        if (System.currentTimeMillis() - timer2 > 500) {
          counter1 += 1;
          timer2 = System.currentTimeMillis();
        }
        // set azimuth to 0
        // every 10s add 20 to the elevation value.
        res = new float[] {180.0f, -90 + counter1};
      }*/ /*if (System.currentTimeMillis() - timer1 > 100000) {
        if (System.currentTimeMillis() - timer2  > 10000) {
          counter2 += 1;
          timer2 = System.currentTimeMillis();
        }
        // set elevation to 0
        // every 10s add 20 to the azimuth value.
        res = new float[] {20 * counter2, 0};
      }*/
      Log.i(TAG, "azimuth: " + res[0] + " elevation: " + 0);
      // keep input volume to 1
      //float inputVolume = 1.0f;*/

      setSpatializerParameters(inputVolume, res[0], 0);



      // ========================================================================================= //
      //                                        keep below
      // ========================================================================================= //

      // Compose the virtual scene with the background. i.e. Draw the virtual scene
      backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR, x0, y0, u, v);

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

  /**
   *
   * @param soundPos
   * @param listenerPos
   * @param camQuat
   * @return float[2]: {azimuth, elevation}
   */
    private float[] processOrientation(float[] soundPos, float[] listenerPos, float[] camQuat) {
      // Calculate the relative position
      float[] relPos = new float[] {
              soundPos[0] - listenerPos[0],
              soundPos[1] - listenerPos[1],
              soundPos[2] - listenerPos[2]
      };

      float[] res = GeometryUtils.calculateOrientation(relPos, camQuat);
      res[0] = (res[0] + 360) % 360;

      return res;
    }

    private float processInputVolume(float[] soundPos,
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

    // Native
    private native void NativeInit(int samplerate, int buffersize, String tempPath);
    private native void OpenFileFromAPK(String path, int offset, int length);
    private native void TogglePlayback();
    private native void onForeground();
    private native void onBackground();
    private native void Cleanup();

  /**
   * Set Spatializer parameters. If you don't want the parameter to change use the forbidden value.
   *
   * @param inputVolume (float): Input volume (gain). Default: 1. Forbidden value: -1.
   * @param azimuth (float): From 0 to 360 degrees. Default: 0. 180 is in front.
   *                Forbidden value: -1.
   * @param elevation (float): -90 to 90 degrees. Default: 0. Forbidden value: -91.
   */
  private native void setSpatializerParameters(float inputVolume, float azimuth, float elevation);
}

