![screenshot](/screenshots/binaural_sound_screenshot.jpg)

## Why this baseapp?
To show how:
- the implementation of the feature binaural sound effect.
- the implementation of the cpp library [SuperPowered](https://github.com/superpoweredSDK/Low-Latency-Android-iOS-Linux-Windows-tvOS-macOS-Interactive-Audio-Platform/tree/master/Examples_Android/SuperpoweredPlayer) into the baseapp.
- the implementation of two cpp library inside an Android Studio project: SuperPowered and the cpp library [freetype](https://freetype.org/index.html) from the [virtual log screen project](../virtual_log/readme.md). 

## Features
- Binaural sound effect.

## What does it do?
- Displays a cube which produces music.
- The sound is spatialized, thus if you move around the cube you'd notice that the sound is coming from it.
- The virtual log screen from the [virtual_log project](../virtual_log/readme.md) can be displayed using the variable ```debugScreenActivated```.

## Used packages
- com.google.ar.core : v1.41.0
- mobilevr : v1.3.0
- SuperPowered (c++): v2.6.3
- freetype (c++): v2.10.0

## Modifications from the original codes hello_ar_java<br>app from ARCore SDK for Android 1.41.0
The logs of differences are here:
Licenses\log_git_diff