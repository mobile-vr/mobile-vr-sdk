#include <jni.h>
#include <ft2build.h>
#include FT_FREETYPE_H
#include <android/log.h>
#include <iostream>

FT_Library initFreeTypeLib() {
    // Initialize FreeType library
    FT_Library library;
    if (FT_Init_FreeType(&library)) {
        __android_log_print(ANDROID_LOG_ERROR, "glyph",
                            "an error occurred during library initialization");
        throw std::runtime_error("an error occurred during library initialization");
    } else {
        return library;
    }
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_myapp_HelloArActivity_loadFont(JNIEnv *env, jobject /*this*/,
                                                       jstring fontPath) {
    const char* font_path = env->GetStringUTFChars(fontPath, nullptr);

    FT_Library library;
    FT_Error error = FT_Init_FreeType(&library);
    if ( error )
    {
        __android_log_print(ANDROID_LOG_ERROR, "glyph",
                            "an error occurred during library initialization");
    }

    FT_Face face;
    if (FT_Init_FreeType(&library) || FT_New_Face(library, font_path,
                                                  0, &face)) {
        // Error handling
        return 0;
    }
    env->ReleaseStringUTFChars(fontPath, font_path);
    return reinterpret_cast<jlong>(face);
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_myapp_HelloArActivity_loadFontFromAssets(JNIEnv *env,
                                                                 jobject /*this*/,
                                                                 jbyteArray font_data) {
    // Convert jbyteArray to byte[]
    jbyte *bytes = env->GetByteArrayElements(font_data, nullptr);
    jsize length = env->GetArrayLength(font_data);

    // Initialize FreeType library
    FT_Library library = initFreeTypeLib();

    FT_Face face;
    // Load font face from memory buffer
    if (FT_New_Memory_Face(library, reinterpret_cast<FT_Byte *>(bytes), length,
                           0, &face)) {
        __android_log_print(ANDROID_LOG_ERROR, "glyph",
                            "an error occurred during FT_New_Memory_Face");
        return 0; // Error handling
    }

    // Clean up
    //FT_Done_FreeType(library);
    env->ReleaseByteArrayElements(font_data, bytes, JNI_ABORT); // Release the byte array
    __android_log_print(ANDROID_LOG_INFO, "glyph",
                        "Load succeeded");

    return reinterpret_cast<jlong>(face);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_myapp_HelloArActivity_get_1num_1glyphs(JNIEnv *env, jobject thiz,
                                                              jlong face) {
    // Cast the jlong parameter back to an FT_Face pointer
    FT_Face facePtr = reinterpret_cast<FT_Face>(face);

    // Check if the face pointer is valid
    if (!facePtr) {
        // Handle invalid face pointer
        return -1; // or any other error code
    }

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "Calling FT_Set_Char_Size from get_num_glyph()....");

    FT_Error error = FT_Set_Char_Size(
            facePtr,    /* handle to face object         */
            0,       /* char_width in 1/64 of points  */
            16 * 64,   /* char_height in 1/64 of points */
            300,     /* horizontal device resolution  */
            300);   /* vertical device resolution    */

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "FT_Set_Char_Size called from get_num_glyph()");

    // Access the num_glyph property of the face object
    return facePtr->num_glyphs;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_myapp_HelloArActivity_getCharacterBitmap(JNIEnv* env,
                                                                 jobject thiz,
                                                                 jlong face,
                                                                 jlong charCode) {
    FT_Error error;

    // Cast the jlong parameter back to an FT_Face pointer
    FT_Face facePtr = reinterpret_cast<FT_Face>(face);

    // Get charCode
    FT_ULong char_code = charCode;

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "charCode : %lu", char_code);

    // Check if the face pointer is valid
    if (!facePtr) {
        // Handle invalid face pointer
        throw std::runtime_error("Invalid face pointer");
    }

    error = FT_Set_Char_Size(
            facePtr,    /* handle to face object         */
            0,       /* char_width in 1/64 of points  */
            16 * 64,   /* char_height in 1/64 of points */
            300,     /* horizontal device resolution  */
            300);   /* vertical device resolution    */

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "FT_Set_Char_Size called");

    if (error) {
        throw std::runtime_error("FT_Set_Char_Size() failed");
    }

    error = FT_Set_Pixel_Sizes(facePtr, 0, 50);

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "FT_Set_Pixel_Sizes called");

    if (error) {
        throw std::runtime_error("FT_Set_Pixel_Sizes() failed");
    }

    FT_UInt glyph_index = FT_Get_Char_Index(facePtr, char_code);

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "FT_Get_Char_Index called");

    if (!glyph_index) {
        throw std::runtime_error("FT_Get_Char_Index() failed");
    }

    error = FT_Load_Glyph(
            facePtr,  /* handle to face object */
            glyph_index,   /* glyph index           */
            0 );  /* load flags, see below */

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "FT_Load_Glyph called");

    if (error) {
        throw std::runtime_error("FT_Load_Glyph() failed");
    }

    // Check if `face->glyph` is not null (for safety)
    if (facePtr->glyph != nullptr) {
        // Access the `format` field of the `FT_GlyphSlotRec` structure
        FT_GlyphSlot glyphSlot = facePtr->glyph;
        int formatValue = glyphSlot->format;

        // Print the value of `format`
        __android_log_print(ANDROID_LOG_ERROR, "glyph",
                            "Value of format: %d\n", formatValue);
    } else {
        __android_log_print(ANDROID_LOG_ERROR, "glyph",
                            "Error: `face->glyph` is null\n");
    }

    // render glyph
    error = FT_Render_Glyph( facePtr->glyph,   /* glyph slot  */
                             FT_RENDER_MODE_NORMAL ); /* render mode */ // FT_RENDER_MODE_GRAY

    // Extract pixel data from glyph slot
    unsigned char* pixels = facePtr->glyph->bitmap.buffer;
    int width = facePtr->glyph->bitmap.width;
    int height = facePtr->glyph->bitmap.rows;

    if (pixels == nullptr) {
        throw std::runtime_error("pixels is a null pointer");
    }

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "glyph data read");

    // Create a byte array to hold pixel data
    jbyteArray result = env->NewByteArray(width * height);

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "NewByteArray called");

    env->SetByteArrayRegion(result, 0, width * height, (jbyte*)pixels);

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "jbyteArray created in java env");

    // Create a new BitmapData object in Java
    jclass bitmapDataClass = env->FindClass("com/mobilevr/log/BitmapData");
    if (!bitmapDataClass) {
        // Error handling
        return NULL;
    }

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "bitmapDataClass created");

    jmethodID constructor = env->GetMethodID(bitmapDataClass, "<init>", "([BII)V");
    if (!constructor) {
        // Error handling
        return NULL;
    }

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "constructor created");

    jobject bitmapObj = env->NewObject(bitmapDataClass, constructor, result, width, height);
    if (!bitmapObj) {
        // Error handling
        return NULL;
    }

    __android_log_print(ANDROID_LOG_ERROR, "glyph",
                        "bitmapObj created");

    // Release the jbyteArray
    //env->ReleaseByteArrayElements(bitmapData, pixels, JNI_ABORT);

    // Clean up, I don't know when to call it.
    // FT_Done_FreeType(library);

    return bitmapObj;
}
