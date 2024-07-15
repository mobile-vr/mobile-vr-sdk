cmake_minimum_required(VERSION 3.4.1)

# convert SDK path to forward slashes on Windows
#file(TO_CMAKE_PATH ${PATH_TO_SUPERPOWERED} PATH_TO_SUPERPOWERED)

project(PlayerExample CXX)

#include_directories(src/main/cpp)
#include_directories(src/main/cpp/Superpowered)

# compile player example to a native library
add_library (
        PlayerExample
        SHARED
        PlayerExample.cpp
        Superpowered/OpenSource/SuperpoweredAndroidAudioIO.cpp
)

# link the native library against the following libraries
target_link_libraries (
        PlayerExample
        log
        android
        OpenSLES
        libSuperpoweredAndroid${ANDROID_ABI}.a
)
