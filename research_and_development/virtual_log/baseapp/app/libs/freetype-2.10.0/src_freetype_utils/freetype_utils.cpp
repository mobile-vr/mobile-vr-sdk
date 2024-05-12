#include <jni.h>
#include <ft2build.h>
#include FT_FREETYPE_H

extern "C" {

JNIEXPORT jlong JNICALL Java_com_example_myapp_FreeTypeUtils_loadFont(JNIEnv* env, jobject thiz, jstring fontPath) {
    const char* font_path = env->GetStringUTFChars(fontPath, nullptr);
    FT_Library library;
    FT_Face face;
    if (FT_Init_FreeType(&library) || FT_New_Face(library, font_path, 0, &face)) {
        // Error handling
        return 0;
    }
    env->ReleaseStringUTFChars(fontPath, font_path);
    return reinterpret_cast<jlong>(face);
}

// Add more JNI wrapper functions as needed
}

