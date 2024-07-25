cmake_minimum_required(VERSION 3.4.1)

# convert SDK path to forward slashes on Windows
set(PATH_TO_SUPERPOWERED "${CMAKE_SOURCE_DIR}/Superpowered")

project(PlayerExample CXX)

include_directories(src/main/cpp)
include_directories(${PATH_TO_SUPERPOWERED})

# compile player example to a native library
add_library (
        PlayerExample
        SHARED
        PlayerExample.cpp
        ${PATH_TO_SUPERPOWERED}/OpenSource/SuperpoweredAndroidAudioIO.cpp
)

# link the native library against the following libraries
target_link_libraries (
        PlayerExample
        log
        android
        OpenSLES
        ${PATH_TO_SUPERPOWERED}/libSuperpoweredAndroid${ANDROID_ABI}.a
)
