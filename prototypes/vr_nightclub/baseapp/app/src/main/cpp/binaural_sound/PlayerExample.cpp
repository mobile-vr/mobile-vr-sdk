#include <jni.h>
#include <string>
#include <android/log.h>
#include "Superpowered/OpenSource/SuperpoweredAndroidAudioIO.h"
#include "Superpowered/Superpowered.h"
#include "Superpowered/SuperpoweredAdvancedAudioPlayer.h"
#include "Superpowered/SuperpoweredSimple.h"
#include "Superpowered/SuperpoweredCPU.h"
#include "SuperpoweredSpatializer.h"
#include <malloc.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <SLES/OpenSLES.h>

#define log_print __android_log_print

static SuperpoweredAndroidAudioIO *audioIO;
static Superpowered::AdvancedAudioPlayer *player;
static Superpowered::Spatializer *spatializer;

// This is called periodically by the audio engine.
static bool audioProcessing (
        void * __unused clientdata, // custom pointer
        short int *audio,           // output buffer
        int numberOfFrames,         // number of frames to process
        int samplerate              // current sample rate in Hz
) {
    // Set SampleRate to the AdvancedAudioPlayer
    player->outputSamplerate = (unsigned int)samplerate;
    float playerOutput[numberOfFrames * 2];
    float playerOutputSpatialized[numberOfFrames * 2];

    // Get player buffer and if player isn't paused, apply Spatializer, and then set it into audio buffer
    if (player->processStereo(playerOutput, false, (unsigned int)numberOfFrames)) {
        spatializer->process(playerOutput, NULL, playerOutputSpatialized, NULL, (unsigned int)numberOfFrames, true);
        Superpowered::FloatToShortInt(playerOutputSpatialized, audio, (unsigned int)numberOfFrames);
        //__android_log_print(ANDROID_LOG_ERROR, "mobilevr",
        //                    "playing: true");
        return true;
    } else {
        //__android_log_print(ANDROID_LOG_ERROR, "mobilevr",
        //                    "playing: false");
        return false;
    }
}

// StartAudio - Start audio engine and initialize player.
extern "C" JNIEXPORT void
Java_com_example_myapp_HelloArActivity_nativeInit(JNIEnv *env, jobject __unused obj, jint samplerate, jint buffersize, jstring tempPath) {
    Superpowered::Initialize("ExampleLicenseKey-WillExpire-OnNextUpdate");

    // setting the temp folder for progressive downloads or HLS playback
    // not needed for local file playback
    const char *str = env->GetStringUTFChars(tempPath, 0);
    Superpowered::AdvancedAudioPlayer::setTempFolder(str);
    env->ReleaseStringUTFChars(tempPath, str);

    // creating the player
    player = new Superpowered::AdvancedAudioPlayer((unsigned int)samplerate, 0);

    audioIO = new SuperpoweredAndroidAudioIO (
            samplerate,                     // device native sampling rate
            buffersize,                     // device native buffer size
            false,                          // enableInput
            true,                           // enableOutput
            audioProcessing,                // process callback function
            NULL,                           // clientData
            -1,                             // inputStreamType (-1 = default)
            SL_ANDROID_STREAM_MEDIA         // outputStreamType (-1 = default)
    );

    // Initialize the spatializer
    spatializer = new Superpowered::Spatializer((unsigned int)samplerate);
}

// OpenFile - Open file in player, specifying offset and length.
extern "C" JNIEXPORT void
Java_com_example_myapp_HelloArActivity_openFileFromAPK (
        JNIEnv *env,
        jobject __unused obj,
        jstring path,       // path to APK file
        jint offset,        // offset of audio file
        jint length         // length of audio file
) {
    const char *str = env->GetStringUTFChars(path, 0);
    player->open(str, offset, length);
    env->ReleaseStringUTFChars(path, str);

    // open file from any path: player->open("file system path to file");
    // open file from network (progressive download): player->open("http://example.com/music.mp3");
    // open HLS stream: player->openHLS("http://example.com/stream");
}

// TogglePlayback - Toggle Play/Pause state of the player.
extern "C" JNIEXPORT void
Java_com_example_myapp_HelloArActivity_togglePlayback(JNIEnv * __unused env, jobject __unused obj) {
    player->togglePlayback();
    Superpowered::CPU::setSustainedPerformanceMode(player->isPlaying()); // prevent dropouts
}

// onBackground - Put audio processing to sleep if no audio is playing.
extern "C" JNIEXPORT void
Java_com_example_myapp_HelloArActivity_onBackground(JNIEnv * __unused env, jobject __unused obj) {
    audioIO->onBackground();
}

// onForeground - Resume audio processing.
extern "C" JNIEXPORT void
Java_com_example_myapp_HelloArActivity_onForeground(JNIEnv * __unused env, jobject __unused obj) {
    audioIO->onForeground();
}

// Cleanup - Free resources.
extern "C" JNIEXPORT void
Java_com_example_myapp_HelloArActivity_cleanup(JNIEnv * __unused env, jobject __unused obj) {
    delete audioIO;
    delete player;
}

// Set spatializer parameters
extern "C" JNIEXPORT void
Java_com_example_myapp_HelloArActivity_setSpatializerParameters(
        JNIEnv *env,
        jobject __unused obj,
        jfloat inputVolume,
        jfloat azimuth,
        jfloat elevation) {
    if (inputVolume != -1) {
        spatializer->inputVolume = inputVolume;
        //__android_log_print(ANDROID_LOG_ERROR, "mobilevr",
        //                    "inputVolume: %f", spatializer->inputVolume);
    }
    if (azimuth != -1) {
        spatializer->azimuth = azimuth;
        //__android_log_print(ANDROID_LOG_ERROR, "mobilevr",
        //                    "azimuth: %f", spatializer->azimuth);
    }
    if (elevation != -91) {
        spatializer->elevation = elevation;
        //__android_log_print(ANDROID_LOG_ERROR, "mobilevr",
        //                    "elevation: %f", spatializer->elevation);
    }

}
