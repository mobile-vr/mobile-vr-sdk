## Message to visitors
Repo is public but wiki isn’t finished.<br>
Advice on good practices is welcome.

## What is mobile-vr-sdk for Android?
Software Development Kit to build XR apps on Android.<br>

## Features
| Features | Status |
|---|---|
| 6-dof (ARCore)   | Developped  |
| 6-dof without ARCore | |
| Hands tracking (mediapipe) | Developped  |
| Rendering (OpenGL) | Developped |
| Splitted screen like VR | Developped |
| Change screen sizes | Developped |
| Joystick support | |
| Multiplayer | |
| Vocal command | |
| Video streaming | |

## Performance
\>30 fps

## Folders presentation
**baseapp** - Starting point to make a VR app with modbilevr sdk.<br>

**external_licenses** - Licenses of the dependencies.<br>

**prototypes** - Demo app to show the sdk's potential.<br>

**research_and_development** - Where to contribute by proposing a sample app<br>
demonstrating the implementation of specific features or tips with shaders.<br>

**screenshots** - repo's images<br>

**sdk** - The com.mobilevr package, the original hello_ar_java app, diff from the<br>
ARCore package with the modified classes, a shader scripts collection.

## Documentation
[wiki](mobile-vr-sdk)

## How to get started with building your app?
Download the release of the baseapp on your computer and start adding your<br>
favorite models, implementing your own VR project, developping new features<br>
and new classes for this sdk.

## What can we make?
![screenshot](/screenshots/basket_ball_game_screenshot.jpg)
[Basket ball app](prototypes/basket_ball_game)

## Limitations
- Hands are not always detected by the ML algorithm. Here are few tips:
  - Avoid having objects with the same color of your hands around you,
  - Show your full hand to the camera,
  - Keep a homogeneous light in your environment.

- Not possible to use more than 1h since the phone is getting hot
- Vertices amount limit (not measured yet)
- ARCore dependant

## Dependencies
- android-material
- androidx
- apache-commons-math
- arcore-android-sdk
- mediapipe
- obj

Licenses: [Licenses\external_dependencies](external_licenses)

## Contact
- mail: mobile.vr@outlook.com
- discord: add me @mobilevr to join the server
