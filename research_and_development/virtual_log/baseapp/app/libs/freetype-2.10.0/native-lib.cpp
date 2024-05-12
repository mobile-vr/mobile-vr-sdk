#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
    JNIEXPORT
jstring Java_com_example_glyph_1font_1v1_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */)
    {
        std::string hello = "Hello from C++";
        return env->NewStringUTF(hello.c_str());
    }
}