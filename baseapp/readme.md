![screenshot](/screenshots/baseapp_screenshot.jpg)

## Why this baseapp?
The baseapp is the starting point before developping a new feature, or creating<br>
a new VR app. It contains all the currently implemented features via the<br>
com.mobilevr package. It also contains guidelines (see the comments "keep<br>
above" and "keep below" sections) in the HelloArActivity.java to help novices<br>
understand how to implement their first objects.

## Features
- 6-dof (ARCore)
- HandsTracking (mediapipe)
- Rendering (OpenGL)
- Splitted screen like VR
- Change screens sizes

## What does it do?
- Display a cube
- Display the pointer at the top of the finger
- Configured for one hand

## Used packages
- com.example.myapp : where the activities are
- com.google.ar.core : the ARCore package
- com.mobilevr : the sdk of this repository

## Dependencies
See [Licenses\external_dependencies](Licenses/external_licenses)


## Modifications from the original codes hello_ar_java<br>app from ARCore SDK for Android 1.41.0
The logs of differences are here:
Licenses\log_git_diff