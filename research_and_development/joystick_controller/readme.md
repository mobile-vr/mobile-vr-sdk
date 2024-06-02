![screenshot](/screenshots/joystick_controller_screenshot.jpg)

## Why this baseapp?
This app is provided so that you can connect your controller, for example xbox360 controller to your mobile, and see the key inputs while being in VR.
On the black window you can see controller data like its AXIS_RZ value, or that the button button A (keycode 96) is being pushed and released.

## Notes
New functions added in the MainActivity to detect user inputs events: dispatchKeyEvent and dispatchGenericMotionEvent.
GameControllerManger class is used in the com.example package to look for input devices and to log user inputs informations onto the virtual log window.

## Requirements
To use a controller with your phone check out this wiki section.

## Feature
- joystick controller

## What does it do?
- Display black window terminal, with the tracked user inputs.

## Used packages
- com.example.myapp : where the activities are
- com.google.ar.core : the ARCore package
- com.mobilevr : the sdk of this repository
- freetype c++ library

## Dependencies
See [Licenses\external_dependencies](Licenses/external_licenses)


## Modifications from the original codes hello_ar_java<br>app from ARCore SDK for Android 1.41.0
The logs of differences are here:
Licenses\log_git_diff