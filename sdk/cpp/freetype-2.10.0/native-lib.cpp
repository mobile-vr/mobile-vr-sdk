#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
    JNIEXPORT
jstring Java_com_example_myapp_HelloArActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */)
    {
        std::string hello = "Hello from C++";
        return env->NewStringUTF(hello.c_str());
    }
}