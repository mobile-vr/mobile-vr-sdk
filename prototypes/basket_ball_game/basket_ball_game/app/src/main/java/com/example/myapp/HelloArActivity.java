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
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Get the detailed modifications at "Licenses/log_git_diff/diff_HelloArActivity.diff"
 * at root of this app.
 */

package com.example.myapp;

import com.baseapp.R;

import static com.mobilevr.utils.GeometryUtils.interpolatePoint;
import static com.mobilevr.utils.GeometryUtils.getFingerQuaternion;
import static com.mobilevr.utils.GeometryUtils.checkTouchingIF;
import static com.mobilevr.utils.QuaternionUtils.convertQuaternionToEulerAngles;
import static com.mobilevr.utils.QuaternionUtils.quaternionToMatrix;
import static com.mobilevr.utils.VectorUtils.concatenateArrays;
import static com.mobilevr.utils.VectorUtils.norm;
import static com.mobilevr.utils.VectorUtils.rotate;
import static com.mobilevr.utils.VectorUtils.rotateVector3D;
import static com.mobilevr.utils.VectorUtils.transformVector;
import static com.mobilevr.utils.VectorUtils.transformVertices;
import static com.mobilevr.utils.PhysicsUtils.calculatePosition;
import static com.mobilevr.utils.PhysicsUtils.calculateNewSpeed;
import com.mobilevr.utils.FloatArrayFIFO;
import com.mobilevr.handstracking.HandsTrackingThread;
import com.mobilevr.handstracking.IntersectionPoint;
import com.mobilevr.designobjects.VirtualObject;
import com.mobilevr.modified.samplerender.arcore.BackgroundRenderer;

import android.content.Context;
import android.media.Image;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
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
import com.mobilevr.modified.samplerender.Framebuffer;
import com.mobilevr.modified.samplerender.Mesh;
import com.mobilevr.modified.samplerender.SampleRender;
import com.mobilevr.modified.samplerender.Shader;
import com.mobilevr.modified.samplerender.Texture;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.solutions.hands.HandLandmark;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.text.DecimalFormat;
import java.util.Map;


public class HelloArActivity extends AppCompatActivity implements SampleRender.Renderer {
  private static final String TAG = "mobilevr";
  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 100f;

  //Rendering. The Renderers are created here, and initialized when the GL surface is created.
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

  // Cube
  private Mesh cubeObjectMesh;
  private Shader cubeObjectShader;

  // Triangle
  private VirtualObject triangleObject;
  private float[] triangleCoords, initialTriangleCoords;

  // Line
  private VirtualObject lineObject;

  // Basket ball
  private Mesh basketBallObjectMesh;
  private Shader basketBallObjectShader;
  private float[] initialBallPosition, ballPosition;
  private float[] initialBallInterfacePosition, ballInterfacePosition;
  private float[] basketBallSpeed = new float[3];
  private FloatArrayFIFO floatArrayFIFO = new FloatArrayFIFO();
  private Boolean startPositionComputing = false, startSpeedComputing = false;
  private float[] initialBallThrownPosition;
  private long startTimePosition;
  private float deltaTimeSpeedComputing=0.1f;
  private float ballRadius;
  private Boolean ballThrown=false;

  // Basket board
  private VirtualObject basketBoardObject;
  private Boolean ballCollidedToBoard=false;
  private float[] moveBasketBoard;

  // Hoop
  private float[] hoopCenterPoint;
  private Mesh hoopObjectMesh;
  private Shader hoopObjectShader;
  private Boolean highEnough=false;
  private float startCollidingDistance, stopCollidingDistance;
  private int score=0;

  // Play - Restart Button
  private VirtualObject playButtonObject;
  private Boolean playButtonDisplayed=true;
  private Texture restartTexture;
  private Boolean startCountdown=false;
  private IntersectionPoint pointIF1, pointIF2;

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
  public static Context nonUiContext;
  private Boolean drawIntersectionPoint, drawPointer, fixCamera;
  private float[] cameraPosition = new float[3];
  private IntersectionPoint pointE;
  private Boolean drawTriangle;
  private DecimalFormat decimalFormat = new DecimalFormat("0.0000");
  private long testTimer;
  private Boolean start=false;
  private long startTime;

  // Hands tracking
  private HandsTrackingThread handsTrackingThread;
  private float fovx, fovy;
  private float[] fingerQuaternion;
  private float[] rotationFingerQuaternion;
  private Boolean fifoHandsPosTimerStarted=false;
  private long fifoHandsPosTimer;
  private FloatArrayFIFO floatArrayHandsPosFIFO = new FloatArrayFIFO();

  // Digits
  private float[] digitsPos;
  private float unitDigit;
  private VirtualObject digitSquareObject;
  private float[] digitPosColor;

  // Digits Timer
  private float[] digitsPosTimer;
  private float unitDigitTimer;
  private VirtualObject digitSquareTimerObject;
  private float[] digitPosTimerColor;

  // Fifo
  private long fifoTimer;
  private Boolean fifoTimerStarted=false;

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

    time = System.currentTimeMillis();
    startTime = System.currentTimeMillis();

    // horizontal and vertical fov
    // Using the phone in portrait, the y is vertical and x is horizontal.
    fovx = 40.0f * (float) (Math.PI / 180);
    fovy = 50.0f * (float) (Math.PI / 180);

    // DEBUG PARAMETERS
    drawIntersectionPoint = false;
    drawPointer = true;
    fixCamera = false;
    if (fixCamera) {
      cameraPosition = new float[] {0, 0, 0};
    }
    drawTriangle = false;

    //Move the basket board and its ball
    moveBasketBoard = new float[]{0, 0, 0};

    // BALL POSITION & QUATERNION init
    initialBallPosition = new float[] {0.0f, 0.0f, -1.0f};
    initialBallPosition[0] += moveBasketBoard[0];
    initialBallPosition[1] += moveBasketBoard[1];
    initialBallPosition[2] += moveBasketBoard[2];
    ballPosition = initialBallPosition;
    ballRadius = 0.075f;

    // BALL INTERFACE POSITION init
    initialBallInterfacePosition  = new float[] {0.0f, 0.0f, -1.15f};
    ballInterfacePosition = initialBallInterfacePosition;

    // HOOP
    // position of the hoop center
    hoopCenterPoint = new float[] {0.0f, 0.6f, -2.75f};
    // define the distance from the hoop's center where the ball starts colliding the hoop
    startCollidingDistance = 0.15f;
    // the distance where the ball stops colliding
    stopCollidingDistance = 0.25f;

    // DIGITS
    unitDigit = 0.05f;
    digitsPos = new float[] {0.35f, 1.4f, -3.0f};
    digitPosColor = new float[]{1.0f, 0.0f, 0.0f, 1.0f};

    // DIGITS Timer
    unitDigitTimer = 0.05f;
    digitsPosTimer = new float[] {-0.35f, 1.4f, -3.0f};
    digitPosTimerColor = new float[]{1.0f, 1.0f, 0.0f, 1.0f};

    // Intersection points with triangleCoords
    pointE = new IntersectionPoint();
    // Intersection points with Play button interface
    pointIF1 = new IntersectionPoint();
    pointIF2 = new IntersectionPoint();
  }

  /**
   * When the app's main thread gets killed.
   */
  @Override
  protected void onDestroy() {
    Log.i(TAG, "onDestroy");

    if (session != null) {
      // Explicitly close ARCore Session to release native resources.
      // Review the API reference for important considerations before calling close() in apps with
      // more complicated lifecycle requirements:
      // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
      session.close();
      session = null;
    }

    /**
     * Interrupt HandsTrackingThread
     */
    if (handsTrackingThread != null) {
      handsTrackingThread.interrupt();
    }

    super.onDestroy();
  }

  /**
   * When the app is launched, or when the user has opened another app without killing
   * the main thread, and gets back on it.
   *
   * Creates and starts a new hands tracking thread.
   */
  @Override
  protected void onResume() {
    Log.i(TAG, "onResume");
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
    handsTrackingThread = new HandsTrackingThread(nonUiContext);
    handsTrackingThread.start();
    Log.i(
            TAG,
            "Starting hands tracking thread: " +
                    Boolean.toString(handsTrackingThread.isRunning)
    );

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
  public void onPause() {
    super.onPause();

    Log.i(TAG, "onPause");

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
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
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
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  /**
   * The best place to create all your objects.
   *
   * Objects:
   *    - Cube: to debug the intersection between line and triangle
   *    - Triangle: the interface (IF) of the ball
   *    - Line: the pointer
   *    - Basket ball
   *    - Basket board
   *    - Hoop
   *    - Start/Restart button
   *
   * @param render : the object drawing on the phone's screen.
   */
  @Override
  public void onSurfaceCreated(SampleRender render) {
    // Prepare the rendering objects. This involves reading shaders and 3D model files, so may throw
    // an IOException.
    try {
      // To render the image taken from the camera at the background
      backgroundRenderer = new BackgroundRenderer(render);
      virtualSceneFramebuffer = new Framebuffer(render, /*width=*/ 1, /*height=*/ 1);

      // Cube Object init - used to debug the intersection between line and IF
      cubeObjectMesh = Mesh.createFromAsset(render, "models/square.obj");
      cubeObjectShader =
          Shader.createFromAssets(
              render,
              "shaders/houseShader.vert", // .vert is for the position
              "shaders/houseShader.frag", // .frag is for the color
              null);

      // TRIANGLE object init
      initialTriangleCoords = new float[]{   // in counterclockwise order:
              -0.35f,  0.3f, 0.0f, // top
              0.35f, 0.3f, 0.0f,  // top right
              0.0f, -0.3f, 0.0f // bottom right
      };
      triangleCoords = initialTriangleCoords;
      int[] triangleIndex = {   // in counterclockwise order:
              0,  1, 2
      };
      int COORDS_PER_VERTEX = 3;
      String vertexShaderFileName = "shaders/houseShader.vert";
      String fragmentShaderFileName = "shaders/houseShader.frag";
      String mode = "simple";
      triangleObject = new VirtualObject(
              render,
              COORDS_PER_VERTEX,
              triangleCoords,
              triangleIndex,
              vertexShaderFileName,
              fragmentShaderFileName,
              null,
              mode);


      // LINE object init: this is the pointer
      float[] lineCoords = {
              // Front face
              -0.005f, -0.005f,  10.0f,
              0.005f, -0.005f,  10.0f,
              0.005f,  0.005f,  10.0f,
              -0.005f,  0.005f,  10.0f,

              // Back face
              -0.005f, -0.005f, -10.0f,
              0.005f, -0.005f, -10.0f,
              0.005f,  0.005f, -10.0f,
              -0.005f,  0.005f, -10.0f
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
      COORDS_PER_VERTEX = 3;
      vertexShaderFileName = "shaders/houseShader.vert";
      fragmentShaderFileName = "shaders/houseShader.frag";
      mode = "simple";
      lineObject = new VirtualObject(
              render,
              COORDS_PER_VERTEX,
              lineCoords,
              lineIndex,
              vertexShaderFileName,
              fragmentShaderFileName,
              null,
              mode);


      // BASKET BALL object init
      basketBallObjectMesh = Mesh.createFromAsset(render, "models/basketBall.obj");
      basketBallObjectShader = Shader.createFromAssets(
              render,
              "shaders/houseShader.vert", // is for the position
              "shaders/houseShader.frag", // fragment is for the color
              null
      );


      // BASKETBOARD object init
      float[] basketBoardCoords = { // counterclock order
              // Front face
              -0.4f, 0.5f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
              -0.4f, 1.1f, -3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
              0.4f, 1.1f, -3.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,
              0.4f, 0.5f, -3.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f
      };
      for (int i=0; i<4; i++) {
        basketBoardCoords[i*8] += moveBasketBoard[0];
        basketBoardCoords[i*8+1] += moveBasketBoard[1];
        basketBoardCoords[i*8+2] += moveBasketBoard[2];
      }
      int[] basketBoardIndex = {
              // Front face
              0, 1, 2,
              0, 2, 3
      };
      COORDS_PER_VERTEX = 8; // 3 for position, 3 for color, 2 for texture coordinates
      vertexShaderFileName = "shaders/textureShader.vert";
      fragmentShaderFileName = "shaders/textureShader.frag";
      mode = "texture";
      basketBoardObject = new VirtualObject(
              render,
              COORDS_PER_VERTEX,
              basketBoardCoords,
              basketBoardIndex,
              vertexShaderFileName,
              fragmentShaderFileName,
              null,
              mode);
      Texture basketBoardTexture = Texture.createFromAsset(
              render,
              "images/basketBoard.png",
              Texture.WrapMode.CLAMP_TO_EDGE,
              Texture.ColorFormat.SRGB);
      basketBoardObject.shader.setTexture("ourTexture", basketBoardTexture);


      // HOOP object init
      // 1.08 as radius, I want 0.2 so I need to scale it by 0.2, later
      hoopObjectMesh = Mesh.createFromAsset(render, "models/hoopObject.obj");
      hoopObjectShader = Shader.createFromAssets(
              render,
              "shaders/houseShader.vert", // is for the position
              "shaders/houseShader.frag", // fragment is for the color
              null
      );


      // Play / Restart button display
      float[] color = {0.0f, 0.0f, 0.0f}; // black

      float[] v1 = {-0.05f, 0.01f, -0.2f};
      float[] tc1 = {0.0f, 1.0f};

      float[] v2 = {-0.05f, 0.03f, -0.2f};
      float[] tc2 = {0.0f, 0.0f};

      float[] v3 = {0.05f, 0.03f, -0.2f};
      float[] tc3 = {1.0f, 0.0f};

      float[] v4 = {0.05f, 0.01f, -0.2f};
      float[] tc4 = {1.0f, 1.0f};

      float[] playButtonIF1 = concatenateArrays(v1, v2, v3);
      float[] playButtonIF2 = concatenateArrays(v1, v3, v4);

      float[][] playButtonIF = {playButtonIF1, playButtonIF2};

      float[] playButtonVertices = concatenateArrays(
              v1, color, tc1,
              v2, color, tc2,
              v3, color, tc3,
              v4, color, tc4
      );
      int[] playButtonIndex = {
              // Front face
              0, 1, 2,
              0, 2, 3
      };
      COORDS_PER_VERTEX = 8;
      vertexShaderFileName = "shaders/textureShader.vert";
      fragmentShaderFileName = "shaders/textureShader.frag";
      mode = "texture";
      playButtonObject = new VirtualObject(
              render,
              COORDS_PER_VERTEX,
              playButtonVertices,
              playButtonIndex,
              vertexShaderFileName,
              fragmentShaderFileName,
              playButtonIF,
              mode);
      // its textures
      Texture startTexture = Texture.createFromAsset(
              render,
              "images/startButton.png",
              Texture.WrapMode.CLAMP_TO_EDGE,
              Texture.ColorFormat.SRGB);
      restartTexture = Texture.createFromAsset(
              render,
              "images/restartButton.png",
              Texture.WrapMode.CLAMP_TO_EDGE,
              Texture.ColorFormat.SRGB);
      playButtonObject.shader.setTexture("ourTexture", startTexture);


      // Numbers SCORE / Countdown
      float[] digitSquareCoords = { // counterclock order
              // Front face
              -1.5f * unitDigit, 0.5f * unitDigit, 0.0f,
              1.5f * unitDigit, 0.5f * unitDigit, 0.0f,
              1.5f * unitDigit, -0.5f * unitDigit, 0.0f,
              -1.5f * unitDigit, -0.5f * unitDigit, 0.0f
      };
      int[] digitSquareIndex = {
              // Front face
              0, 1, 2,
              0, 2, 3
      };
      COORDS_PER_VERTEX = 3;
      vertexShaderFileName = "shaders/houseShader.vert";
      fragmentShaderFileName = "shaders/houseShader.frag";
      mode = "simple";
      digitSquareObject = new VirtualObject(
              render,
              COORDS_PER_VERTEX,
              digitSquareCoords,
              digitSquareIndex,
              vertexShaderFileName,
              fragmentShaderFileName,
              null,
              mode);


      // Numbers Timer
      float[] digitSquareCoordsTimer = { // counterclock order
              // Front face
              -1.5f * unitDigitTimer, 0.5f * unitDigitTimer, 0.0f,
              1.5f * unitDigitTimer, 0.5f * unitDigitTimer, 0.0f,
              1.5f * unitDigitTimer, -0.5f * unitDigitTimer, 0.0f,
              -1.5f * unitDigitTimer, -0.5f * unitDigitTimer, 0.0f
      };
      int[] digitSquareIndexTimer = {
              // Front face
              0, 1, 2,
              0, 2, 3
      };
      COORDS_PER_VERTEX = 3;
      vertexShaderFileName = "shaders/houseShader.vert";
      fragmentShaderFileName = "shaders/houseShader.frag";
      mode = "simple";
      digitSquareTimerObject = new VirtualObject(
              render,
              COORDS_PER_VERTEX,
              digitSquareCoordsTimer,
              digitSquareIndexTimer,
              vertexShaderFileName,
              fragmentShaderFileName,
              null,
              mode);


    } catch (IOException e) {
      Log.e(TAG, "Failed to read a required asset file", e);
      messageSnackbarHelper.showError(
              this,
              "Failed to read a required asset file: " + e
      );
    }
  }

  /**
   * Resize the image displayed.
   *
   * @param render
   * @param width
   * @param height
   */
  @Override
  public void onSurfaceChanged(SampleRender render, int width, int height) {
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
  public void onDrawFrame(SampleRender render) {
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
      cameraPosition = new float[] {
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
    List<LandmarkProto.NormalizedLandmark> landmarkList = handsTrackingThread.getLandmarkList();

    // Get fingerQuaternion and rotationFingerQuaternion
    if (landmarkList != null) {
      fingerQuaternion = getFingerQuaternion(cameraPose, landmarkList, fovx, fovy);
      rotationFingerQuaternion = quaternionToMatrix(fingerQuaternion);
    } else {
      fingerQuaternion = null;
      rotationFingerQuaternion = null;
    }

    // CHECK INTERSECTION between finger direction and triangle
    if (!ballThrown) {
      checkTouchingIF(cameraPose, landmarkList, triangleCoords, pointE, fingerQuaternion);
    } else {
      rotationFingerQuaternion = null;
      fingerQuaternion = null;
    }


    // If:
    //    - the ball is grabbed
    //    - the position computing hasn't started
    //    - the ball hasn't been thrown
    //    - the game hasn't started
    //
    // Change BALL position when grabbed (pointE.action = true)
    if (pointE != null && pointE.action && !startPositionComputing && !ballThrown && start
          && rotationFingerQuaternion != null) {

      // Make the new Pose for the Ball and for its surface detection
      ballPosition = transformVector(
              initialBallPosition,
              rotationFingerQuaternion
      );
      ballInterfacePosition = transformVector(
              initialBallInterfacePosition,
              rotationFingerQuaternion
      );

      // Add player position
      ballPosition[0] += cameraPose.tx();
      ballPosition[1] += cameraPose.ty();
      ballPosition[2] += cameraPose.tz();

      // Start computing the speed of the ball while the user moves it
      startSpeedComputing = true;
    }

    // Saves the ball Position in a FIFO
    addPosition(ballPosition);


    // If:
    //    - the script can start computing the speed
    //    - the ball hasn't been thrown
    if (startSpeedComputing && pointE != null && !ballThrown && landmarkList != null) {
      Log.i(TAG, "Grabbed");
      float myX = landmarkList.get(HandLandmark.INDEX_FINGER_TIP).getX();
      float myY = landmarkList.get(HandLandmark.INDEX_FINGER_TIP).getY();

      float myX2 = myX - 0.5f;
      float myY2 = -myY + 0.5f;

      float[] fingerPos = new float[]{myX2, myY2, 0};

      addHandsPosition(fingerPos);

      if (basketBallSpeed != null && !pointE.action) {

        // Set speed using the fifo
        float[] startPosition = floatArrayHandsPosFIFO.getFirst();

        // Compute speed with more speed in the direction the camera is looking
        computeSpeed(
                startPosition,
                fingerPos,
                null,
                0,
                cameraPose
        );

        testTimer = System.currentTimeMillis();

        // Prepare data for position generation
        initialBallThrownPosition = ballPosition;

        // Change SITL state
        startSpeedComputing = false;
        startPositionComputing = true;
        ballThrown = true;
        startTimePosition = System.currentTimeMillis();
        /*Log.i(TAG, "Ball thrown with speed : " +
                decimalFormat.format(basketBallSpeed[0]) + " " +
                decimalFormat.format(basketBallSpeed[1]) + " " +
                decimalFormat.format(basketBallSpeed[2]));*/
      }
    }


    if (startPositionComputing) {

      // Earth's gravity field in m/s^2
      float[] gravity = new float[] {
              0.0f,
              -9.81f,
              0.0f
      };
      // Ball's mass
      float mass = 0.1f;

      ballPosition = calculatePosition(
              initialBallThrownPosition,
              basketBallSpeed,
              (float) ( (System.currentTimeMillis() - startTimePosition) * Math.pow(10, -3) ),
              mass,
              gravity
      );
    }

    if (testTimer != 0 && System.currentTimeMillis() - testTimer > 4500) {
      restartGame();
    }


    // The ball touches the floor
    if (ballPosition[1] < -0.5) {
      Log.i(TAG, "Ball below floor");
      // Reset ballPosition
      ballPosition[1] = -0.49f;
      // Get new startTimePosition
      startTimePosition = System.currentTimeMillis();
      // change initial speed
      float[] floorNormalVector = new float[]{0.0f, 1.0f, 0.0f};

      computeSpeed(
              floatArrayFIFO.getFirst(),
              ballPosition,
              null,
              1,
              null
      );

      basketBallSpeed = calculateNewSpeed(basketBallSpeed, floorNormalVector, 0.8f);

      // change initial position
      initialBallThrownPosition = ballPosition;
    }

    // Basket hoop collision behavior
    // If the ball climbs higher than the hoop
    if (ballPosition[1] > (hoopCenterPoint[1] + ballRadius) && !highEnough) {
      // sets the variable highEnough to true
      highEnough = true;
    }


    // If:
    //  - highEnough is true and the ball is below the hoop's Y,
    //  - the size of the floatArray holding the ball position is higher than 0. Because it
    //    was getting in when the games starts.
    //  - the game has started
    // this means there is a potential goal.
    if (highEnough && ballPosition[1] < hoopCenterPoint[1] &&
            floatArrayFIFO.size() > 1 && start) {

      // get the 2 last points recorded in the fifo
      float[][] points = floatArrayFIFO.getTwoLast();

      // get the point of the ball's trajectory at y = hoop's y
      float[] point = interpolatePoint(points[0], points[1], hoopCenterPoint[1]);

      // calculate the distance from the hoop's center and this point
      float[] distVec = new float[] {
              hoopCenterPoint[0] - point[0],
              hoopCenterPoint[1] - point[1],
              hoopCenterPoint[2] - point[2]
      };
      float dist = norm(distVec);

      // if the distance is below than startCollidingDistance
      if (dist < startCollidingDistance) {

        // don't do anything and score +1
        score += 1;
        highEnough = false;
      }

      // if the distance is between the 2 dist
      if (dist >= startCollidingDistance && dist <= stopCollidingDistance) {

        // Define the normal vector of the collision
        float[] normalVector = new float[] {0.0f, 1.0f, 0.0f};

        // Calculate the rotation to do to the normalVector for the bounce
        // get d = stop - dist
        float d = stopCollidingDistance - dist;
        // get dMax
        float dMax = stopCollidingDistance - startCollidingDistance;
        // alpha = pi / dMax * d - pi / 2
        float alpha = (float) Math.PI / dMax * d - (float) Math.PI / 2;

        // Using the vector from the hoop center to ball position: distVec
        // get the rotation axis of the normalVector, which is perpendicular to
        // distVec and on the same Y plan
        float[] axis = new float[] {
                 distVec[0],
                 distVec[1],
                -distVec[2]
        };

        // Apply the rotation to the normal vector
        normalVector = rotateVector3D(normalVector, axis, alpha);

        // collide with calculateNewSpeed()
        computeSpeed(
                floatArrayFIFO.getFirst(),
                ballPosition,
                null,
                1,
                null
        );

        basketBallSpeed = calculateNewSpeed(basketBallSpeed, normalVector, 0.8f);

        initialBallThrownPosition = ballPosition;

        // Get new startTimePosition
        startTimePosition = System.currentTimeMillis();

        // Actualize highEnough
        highEnough = false;

        // Allow the ball to hit the board again
        ballCollidedToBoard = false;
      }
    }


    // Basket board collision behavior
    float xLimitDown = -0.4f + moveBasketBoard[0] - ballRadius;
    float xLimitUp   =  0.4f + moveBasketBoard[0] + ballRadius;
    float yLimitDown =  0.5f + moveBasketBoard[1] - ballRadius;
    float yLimitUp   =  1.1f + moveBasketBoard[1] + ballRadius;
    float zLimitDown = -3.0f + moveBasketBoard[2] + ballRadius;
    float zLimitUp   = -3.0f + moveBasketBoard[2] - ballRadius;

    if (ballPosition[0] > xLimitDown && ballPosition[0] < xLimitUp &&
            ballPosition[1] > yLimitDown && ballPosition[1] < yLimitUp &&
            ballPosition[2] < zLimitDown && ballPosition[2] >= zLimitUp &&
            !ballCollidedToBoard && ballThrown) {

      // change initial speed
      float[] basketBoardNormalVector = new float[]{0.0f, 0.0f, 1.0f};

      computeSpeed(
              floatArrayFIFO.getFirst(),
              ballPosition,
              null,
              1,
              null
      );

      basketBallSpeed = calculateNewSpeed(basketBallSpeed, basketBoardNormalVector, 0.8f);

      // change initial position
      initialBallThrownPosition = ballPosition;

      // Get new startTimePosition
      startTimePosition = System.currentTimeMillis();

      ballCollidedToBoard = true;
    }


    // If the play button should be displayed
    if (playButtonDisplayed) {

      // DRAW PLAY BUTTON

      // applying transformations:
      Matrix.setIdentityM(modelMatrix, 0);
      //Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, 0.0f);
      //Matrix.scaleM(modelMatrix, 0, 1.5f, 1.5f, 1.5f);
      //Matrix.rotateM(modelMatrix, 0, -45f, 0, 0, -1.0f);
      Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

      // setting the color - no "ourColor" in textureShader.vert & textureShader.frag
      //playButtonObject.shader.setVec4("ourColor", new float[]{0.0f, 0.0f, 0.0f, 1.0f});

      // Setting the position, scale and orientation to the square
      playButtonObject.shader.setMat4("uMVPMatrix", uMVPMatrix);

      // drawing the button
      render.draw(playButtonObject.mesh, playButtonObject.shader, virtualSceneFramebuffer, 0, x0, y0, u, v);
      render.draw(playButtonObject.mesh, playButtonObject.shader, virtualSceneFramebuffer, 1, x0, y0, u, v);

      // Check if the user is pinching the vertices of the play button
      checkTouchingIF(cameraPose, landmarkList, playButtonObject.IFs[0], pointIF1, fingerQuaternion);
      checkTouchingIF(cameraPose, landmarkList, playButtonObject.IFs[1], pointIF2, fingerQuaternion);

      if (
              (pointIF1 != null && pointIF2 != null) &&
                      (pointIF1.action || pointIF2.action) &&
                      (System.currentTimeMillis() - startTime > 1500)
      ) {
        startCountdown = true;
        playButtonDisplayed = false;
        playButtonObject.shader.setTexture("ourTexture", restartTexture);
        startTime = System.currentTimeMillis();
        restartGame();
        score = 0;
      }
    }


    // Draw the ball if the game has started
    if (start) {
      drawBall(cameraPose);
    }


    // DRAW BASKET BOARD

    // applying transformations:
    Matrix.setIdentityM(modelMatrix, 0);
    //Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, 0.0f);
    //Matrix.scaleM(modelMatrix, 0, 1.5f, 1.5f, 1.5f);
    //Matrix.rotateM(modelMatrix, 0, -45f, 0, 0, -1.0f);
    Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

    // setting the color - no "vColor" in textureShader.vert & textureShader.frag
    //basketBoardObject.shader.setVec4("vColor", new float[]{0.8f, 0.8f, 0.8f, 1.0f});

    // Setting the position, scale and orientation to the square
    basketBoardObject.shader.setMat4("uMVPMatrix", uMVPMatrix);

    // drawing the basket Board
    render.draw(basketBoardObject.mesh, basketBoardObject.shader, virtualSceneFramebuffer, 0, x0, y0, u, v);
    render.draw(basketBoardObject.mesh, basketBoardObject.shader, virtualSceneFramebuffer, 1, x0, y0, u, v);


    // DRAW HOOP

    // applying transformations:
    Matrix.setIdentityM(modelMatrix, 0);
    Matrix.translateM(modelMatrix, 0, hoopCenterPoint[0], hoopCenterPoint[1], hoopCenterPoint[2]);
    Matrix.scaleM(modelMatrix, 0, 0.2f, 0.2f, 0.2f);
    //Matrix.rotateM(modelMatrix, 0, -45f, 0, 0, -1.0f);
    Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

    // setting the color
    hoopObjectShader.setVec4("vColor", new float[]{0.8f, 0.0f, 0.0f, 1.0f});

    // Setting the position, scale and orientation to the square
    hoopObjectShader.setMat4("uMVPMatrix", uMVPMatrix);

    // drawing the hoop
    render.draw(hoopObjectMesh, hoopObjectShader, virtualSceneFramebuffer, 0, x0, y0, u, v);
    render.draw(hoopObjectMesh, hoopObjectShader, virtualSceneFramebuffer, 1, x0, y0, u, v);


    // DRAW SCORE & COUNT DOWN DIGITS

    int digit1, digit2;
    if (startCountdown) {
      digit2 = 0;
      if (System.currentTimeMillis() - startTime < 1000) {
        digit1 = 3;
      } else if (System.currentTimeMillis() - startTime < 2000) {
        digit1 = 2;
      } else if (System.currentTimeMillis() - startTime < 3000) {
        digit1 = 1;
      } else {
        digit1 = 0;
        startCountdown = false;
        start = true;
        startTime = System.currentTimeMillis();
      }
    } else {
      // Define the number of each digit by doing modulo and / inside a int
      digit1 = score % 10;
      digit2 = score / 10;
    }
    drawDigits(
            digit2,
            digit1,
            digitsPos,
            digitSquareObject,
            unitDigit,
            digitPosColor
    );


    // DRAW Timer DIGITS

    int digitTimer1=0, digitTimer2=0;
    if (start) {
      int timeLeft = (int)( (61000 - (int)(System.currentTimeMillis() - startTime)) / 1000 );
      Log.i(TAG, "timeLeft : " + timeLeft);
      digitTimer1 = timeLeft % 10;
      digitTimer2 = timeLeft / 10;
      if (System.currentTimeMillis() - startTime > 61000) {
        restartAllGame();
      }
    }
    drawDigits(
            digitTimer2,
            digitTimer1,
            digitsPosTimer,
            digitSquareTimerObject,
            unitDigitTimer,
            digitPosTimerColor
    );


    // If the intersection point called pointE is wanted to be drawn
    if (drawIntersectionPoint && pointE != null) {

      // DRAW SQUARE At the E

      // applying transformations:
      Matrix.setIdentityM(modelMatrix, 0);
      Matrix.translateM(
              modelMatrix,
              0,
              pointE.point[0],
              pointE.point[1],
              pointE.point[2]
      );
      Matrix.scaleM(modelMatrix, 0, 0.01f, 0.01f, 0.01f);
      //Matrix.rotateM(modelMatrix, 0, -45f, 0, 0, -1.0f);
      Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

      // setting the color
      cubeObjectShader.setVec4("vColor", new float[]{0.63671875f, 0.76953125f, 0.0f, 1.0f});

      // Setting the position, scale and orientation to the square
      cubeObjectShader.setMat4("uMVPMatrix", uMVPMatrix);

      // drawing the square
      render.draw(cubeObjectMesh, cubeObjectShader, virtualSceneFramebuffer, 0, x0, y0, u, v);
      render.draw(cubeObjectMesh, cubeObjectShader, virtualSceneFramebuffer, 1, x0, y0, u, v);
    }


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

    // Display the latency of the hands tracking thread, and Hands landmark
    if (handsTrackingThread != null) {
      String msg = handsTrackingThread.latency + "ms ";
      //Log.i(TAG, "handsTracking latency: " + msg);
    }

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
  private void configureSession() {
    Config config = session.getConfig();
    config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
      config.setDepthMode(Config.DepthMode.AUTOMATIC);
    } else {
      config.setDepthMode(Config.DepthMode.DISABLED);
    }
    java.util.List<com.google.ar.core.CameraConfig> cameraConfigList = session.
            getSupportedCameraConfigs(new CameraConfigFilter(session));
    for (int i=0;i<cameraConfigList.size();i++) {
      //Log.i(TAG, "i = " + i + ": " + cameraConfigList.get(i).getFpsRange().toString());
    }
    //session.setCameraConfig(cameraConfig);
    session.configure(config);
  }

  /**
   * Draw the basket ball object.
   *
   * @param cameraPose
   */
  private void drawBall(Pose cameraPose) {

    // DRAW BASKET BALL

    // applying transformations:
    Matrix.setIdentityM(modelMatrix, 0);
    Matrix.translateM(modelMatrix, 0, ballPosition[0], ballPosition[1], ballPosition[2]);
    Matrix.scaleM(modelMatrix, 0, 0.3f, 0.3f, 0.3f);
    //Matrix.rotateM(modelMatrix, 0, -45f, 0, 0, -1.0f);

    // Combine the ProjectionView matrix with the Model matrix
    Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

    // setting the color
    basketBallObjectShader.setVec4("vColor", new float[]{1.0f, 0.5f, 0.0f, 1.0f});

    // Setting the position, scale and orientation to the ball
    basketBallObjectShader.setMat4("uMVPMatrix", uMVPMatrix);

    // drawing the basket ball
    render.draw(
            basketBallObjectMesh,
            basketBallObjectShader,
            virtualSceneFramebuffer,
            0, x0, y0, u, v
    );
    render.draw(
            basketBallObjectMesh,
            basketBallObjectShader,
            virtualSceneFramebuffer,
            1, x0, y0, u, v
    );


    // DRAW the ball's triangle interface

    // applying transformations:
    Matrix.setIdentityM(modelMatrix, 0);
    Matrix.translateM(modelMatrix, 0, ballInterfacePosition[0], ballInterfacePosition[1],
            ballInterfacePosition[2]);
    //Matrix.scaleM(modelMatrix, 0, 0.1f, 0.1f, 0.1f);
    //Matrix.rotateM(modelMatrix, 0, -45f, 0, 1.0f, 0);

    // Apply the rotation matrix to your model matrix
    // Note: this is necessary since when the user moves around this triangle interface
    //       if the triangle doesn't orient itself at the user, the user won't see it at 90°.
    if (rotationFingerQuaternion != null) {
      Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotationFingerQuaternion, 0);
    }

    // Combine the ProjectionView matrix with the Model matrix
    Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

    // Move the vertices of the triangle interface
    triangleCoords = transformVertices(initialTriangleCoords, modelMatrix);

    // setting the color
    triangleObject.shader.setVec4("vColor", new float[]{1.0f, 0.0f, 0.0f, 0.5f});

    // Setting the position, scale and orientation to the triangle
    triangleObject.shader.setMat4("uMVPMatrix", uMVPMatrix);

    // drawing the triangle
    if (drawTriangle) {
      render.draw(triangleObject.mesh, triangleObject.shader, virtualSceneFramebuffer, 0, x0, y0, u, v);
      render.draw(triangleObject.mesh, triangleObject.shader, virtualSceneFramebuffer, 1, x0, y0, u, v);
    }
  }

  /**
   * Draw two digit objects side by side.
   *
   * @param digit1: the number of the right digit. It should be between 0 and 9.
   * @param digit2: the number of the left digit. It should be between 0 and 9.
   * @param digitPos: position where to put the two digits side by side.
   * @param digitObject: the VirtualObject of the digit.
   * @param myUnitDigit: the unit to define the size of the digits.
   * @param color: the color of the digits.
   */
  private void drawDigits(
          int digit1,
          int digit2,
          float[] digitPos,
          VirtualObject digitObject,
          float myUnitDigit,
          float[] color
  ) {
    if (digit1 < 0) {
      throw new Error("digit1 argument is below 0: " + digit1);
    }
    if (digit1 > 9) {
      throw new Error("digit1 argument is higher than 9: " + digit1);
    }
    if (digit2 < 0) {
      throw new Error("digit2 argument is below 0: " + digit2);
    }
    if (digit2 > 9) {
      throw new Error("digit2 argument is higher than 9: " + digit2);
    }

    float PI = 180.0f;
    Map<String, float[]> posOrientDigSq = new HashMap<>();
    float distanceBtwDig = myUnitDigit * 3;
    float[] digitPos1 = {digitPos[0] - distanceBtwDig, digitPos[1], digitPos[2]};
    float[] digitPos2 = {digitPos[0] + distanceBtwDig, digitPos[1], digitPos[2]};

    // Create a table with the position and orientation of each digitSquare
    posOrientDigSq.put("A", new float[] {0, 4.0f * myUnitDigit, 0});
    posOrientDigSq.put("B", new float[] {2.0f * myUnitDigit, 2.0f * myUnitDigit, PI / 2});
    posOrientDigSq.put("C", new float[] {2.0f * myUnitDigit, -2.0f * myUnitDigit, PI / 2});
    posOrientDigSq.put("D", new float[] {0, -4.0f * myUnitDigit, 0});
    posOrientDigSq.put("E", new float[] {-2.0f * myUnitDigit, -2.0f * myUnitDigit, PI / 2});
    posOrientDigSq.put("F", new float[] {-2.0f * myUnitDigit, 2.0f * myUnitDigit, PI / 2});
    posOrientDigSq.put("G", new float[] {0, 0, 0});

    // Create a table of the ids A to G to draw given a number from 0 to 9
    String[][] digitToSquares = new String[][] {
            // 0
            new String[] {"A", "B", "C", "D", "E", "F"},
            // 1
            new String[] {"B", "C"},
            // 2
            new String[] {"A", "B", "D", "E", "G"},
            // 3
            new String[] {"A", "B", "C", "D", "G"},
            // 4
            new String[] {"B", "C", "F", "G"},
            // 5
            new String[] {"A", "C", "D", "F", "G"},
            // 6
            new String[] {"A", "C", "D", "E", "F", "G"},
            // 7
            new String[] {"A", "B", "C"},
            // 8
            new String[] {"A", "B", "C", "D", "E", "F", "G"},
            // 9
            new String[] {"A", "B", "C", "D", "F", "G"}
    };

    // Draw first digit
    for (String squareId : digitToSquares[digit1]) {
      float[] pos = posOrientDigSq.get(squareId);

      // applying transformations:
      Matrix.setIdentityM(modelMatrix, 0);
      Matrix.translateM(
              modelMatrix,
              0,
              pos[0] + digitPos1[0],
              pos[1] + digitPos1[1],
              digitPos1[2]);
      //Matrix.scaleM(modelMatrix, 0, 1.5f, 1.5f, 1.5f);
      Matrix.rotateM(modelMatrix, 0, pos[2], 0, 0, -1.0f);
      Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

      // setting the color
      digitObject.shader.setVec4("vColor", color);

      // Setting the position, scale and orientation to the square
      digitObject.shader.setMat4("uMVPMatrix", uMVPMatrix);

      // drawing the square
      render.draw(digitObject.mesh, digitObject.shader, virtualSceneFramebuffer, 0, x0, y0, u, v);
      render.draw(digitObject.mesh, digitObject.shader, virtualSceneFramebuffer, 1, x0, y0, u, v);
    }

    // Draw second digit
    for (String squareId : digitToSquares[digit2]) {
      float[] pos = posOrientDigSq.get(squareId);

      // applying transformations:
      Matrix.setIdentityM(modelMatrix, 0);
      Matrix.translateM(
              modelMatrix,
              0,
              pos[0] + digitPos2[0],
              pos[1] + digitPos2[1],
              digitPos2[2]
      );
      //Matrix.scaleM(modelMatrix, 0, 1.5f, 1.5f, 1.5f);
      Matrix.rotateM(modelMatrix, 0, pos[2], 0, 0, -1.0f);
      Matrix.multiplyMM(uMVPMatrix, 0, vPMatrix, 0, modelMatrix, 0);

      // setting the color
      digitObject.shader.setVec4("vColor", color);

      // Setting the position, scale and orientation to the square
      digitObject.shader.setMat4("uMVPMatrix", uMVPMatrix);

      // drawing the square
      render.draw(digitObject.mesh, digitObject.shader, virtualSceneFramebuffer, 0, x0, y0, u, v);
      render.draw(digitObject.mesh, digitObject.shader, virtualSceneFramebuffer, 1, x0, y0, u, v);
    }
  }

  /**
   * Add a position to the ball's fifo (First In First Out).
   *
   * @param position: ball's position, a float[3]
   */
  private void addPosition(float[] position) {
    if (!fifoTimerStarted) {
      fifoTimer = System.currentTimeMillis();
      fifoTimerStarted = true;
    }
    // Add the new position in the queue
    floatArrayFIFO.enqueue(position);
    // after 200ms of action true, start removing the last element from queue
    if (System.currentTimeMillis() - fifoTimer > (deltaTimeSpeedComputing * 1000)) {
      floatArrayFIFO.dequeue();
    }
  }

  /**
   * Add a position to the hands' fifo.
   * (Can be mixed with addPosition)
   *
   * @param position: hands' position, a float[3]
   */
  private void addHandsPosition(float[] position) {
    if (!fifoHandsPosTimerStarted) {
      fifoHandsPosTimer = System.currentTimeMillis();
      fifoHandsPosTimerStarted = true;
    }
    // Add the new position in the queue
    if (position != null) {
      floatArrayHandsPosFIFO.enqueue(position);
    }
    // after deltaTime of action true, start removing the last element from queue
    if (System.currentTimeMillis() - fifoHandsPosTimer > (deltaTimeSpeedComputing * 1000)) {
      floatArrayHandsPosFIFO.dequeue();
    }
  }

  /**
   * Compute the speed using two positions and a deltaTime.
   *
   * @param startPosition: float[3]
   * @param endPosition: float[3]
   * @param moreSpeed: float[3]
   * @param mode: 0 is for using hands positions from HandsTrackingThread.
   *              1 is for using the ball position in the scene.
   */
  private void computeSpeed(
          float[] startPosition,
          float[] endPosition,
          float[] moreSpeed,
          int mode,
          Pose cameraPose
  ) {
    // Mode using the ball's position
    if (mode == 0) {

      //Get the finger's speed on the screen (X, Y, 0)
      float xSpeed = endPosition[0] - startPosition[0];
      float ySpeed = endPosition[1] - startPosition[1];
      float zSpeed = -0.9f;

      //Get the camera's rotation matrix
      float[] camQuat = cameraPose.getRotationQuaternion();

      // Get the Z rotation
      float[] eulerAngles = convertQuaternionToEulerAngles(camQuat, "RADIANS");

      // Take out the native physical camera's rotation according to the Z axis
      // Note: landscape mode and charger to the right
      float[] fingerSpeed = rotate(
              new float[] {xSpeed, ySpeed},
              eulerAngles[2]
      );

      // X speed using the value from the screen times the new X camera axis
      // Take the X axis of the camera without the Y component
      float[] xAxis = cameraPose.getXAxis();
      // Normalize the xAxis
      float xNorm = (float) Math.sqrt( Math.pow(xAxis[0], 2) + Math.pow(xAxis[2], 2) );
      xAxis[0] = xAxis[0] / xNorm * fingerSpeed[0];
      xAxis[1] = 0;
      xAxis[2] = xAxis[2] / xNorm * fingerSpeed[0];

      // Same with the Z axis
      // Take the Z axis of the camera
      float[] zAxis = cameraPose.getZAxis();
      // Make the zSpeed
      zAxis[0] *= zSpeed;
      zAxis[1] *= zSpeed;
      zAxis[2] *= zSpeed;

      // and the Y speed from the screen times the Y axis in World Coordinate system.
      float[] yAxis = new float[] {0, fingerSpeed[1], 0};

      // Make the basketBallSpeed
      basketBallSpeed[0] = xAxis[0] + yAxis[0] + zAxis[0];
      basketBallSpeed[1] = ( xAxis[1] + yAxis[1] + zAxis[1] ) * 1.7f;
      basketBallSpeed[2] = xAxis[2] + yAxis[2] + zAxis[2];
    } else if (mode == 1){
      // Calculate speed based on the positions
      // Speed = (Distance) / (Time)
      for (int i = 0; i < 3; i++) {
        basketBallSpeed[i] = (endPosition[i] - startPosition[i]) / deltaTimeSpeedComputing;
      }

    } else {
      throw new Error("The mode argument doesn't exist.");
    }

    // Add more speed if provided
    if (moreSpeed != null) {
      basketBallSpeed[0] += moreSpeed[0];
      basketBallSpeed[1] += moreSpeed[1];
      basketBallSpeed[2] += moreSpeed[2];
    }
  }

  /**
   * Once the user throw the ball, this function is used to reset the ball at its initial position
   * so that the user can start scoring again. It also resets all the parameters.
   */
  private void restartGame() {
    Log.i(TAG, "Restarted");
    startPositionComputing = false;
    startSpeedComputing = false;
    ballPosition = initialBallPosition;
    ballInterfacePosition = initialBallInterfacePosition;
    fifoTimerStarted = false;
    fifoHandsPosTimerStarted = false;
    floatArrayFIFO = new FloatArrayFIFO();
    ballCollidedToBoard = false;
    testTimer = 0;
    highEnough = false;
    ballThrown = false;
  }

  /**
   * Reset the hole game and change the parameter to display the play button.
   */
  private void restartAllGame() {
    start = false;
    startTime = System.currentTimeMillis();
    playButtonDisplayed = true;
    pointIF1 = new IntersectionPoint();
    pointIF2 = new IntersectionPoint();
    ballThrown = false;
  }
}

