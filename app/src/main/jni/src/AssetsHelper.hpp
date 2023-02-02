#pragma once

#include "imgui.h"
#include <GLES3/gl3.h>
#include <imgui_internal.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <jni.h>
#include <malloc.h>

#define STB_IMAGE_IMPLEMENTATION
#define STB_IMAGE_RESIZE_IMPLEMENTATION
#include <stb/stb_image.h>
#include <stb/stb_image_resize.h>

// Retrive data from assets
inline int GetAssetData(AAssetManager *asset_manager, const char *filename, void **out) {
    int size = 0;
    AAsset *asset_descriptor = AAssetManager_open(asset_manager, filename, AASSET_MODE_BUFFER);
    if (!asset_descriptor)
        return -1;

    size = AAsset_getLength(asset_descriptor);
    *out = IM_ALLOC(size);
    int64_t num_bytes_read = AAsset_read(asset_descriptor, *out, size);
    AAsset_close(asset_descriptor);
    IM_ASSERT(num_bytes_read == size);
    return size;
}
// Get buffer from assets
inline const void *GetAssetData(AAssetManager *asset_manager, const char *filename, int *buffer_size) {
    AAsset *asset_descriptor = AAssetManager_open(asset_manager, filename, AASSET_MODE_BUFFER);
    if (!asset_descriptor)
        return nullptr;
    
    *buffer_size = AAsset_getLength(asset_descriptor);
    return AAsset_getBuffer(asset_descriptor);
}

inline bool LoadTextureFromAssets(AAssetManager *mgr, const char *filename, GLuint *out, int *out_width, int *out_height, ImVec2 resize = ImVec2(0,0)) {
    int image_width = 0;
    int image_height = 0;
    int chanel = 0;

    int buffer_size;
    const void *buff = GetAssetData(mgr, filename, &buffer_size);
    if (buffer_size <= 0)
        return false;

    unsigned char *image_data = stbi_load_from_memory((unsigned char *)buff, buffer_size, &image_width, &image_height, &chanel, 4);
    if (image_data == NULL)
        return false;

    bool _m = false;
    if (resize.x > 0 && resize.y > 0) {
        int x = (int)resize.x;
        int y = (int)resize.y;
        unsigned char *newImageData = (unsigned char *)malloc(x * y * chanel);
        stbir_resize_uint8(image_data, image_width, image_height, 0, newImageData, x, y, 0, chanel);
        image_width = x;
        image_height = y;
        stbi_image_free(image_data);
        image_data = newImageData;
        _m = true;
    }

    // Create a OpenGL texture identifier
    GLuint image_texture;
    glGenTextures(1, &image_texture);
    glBindTexture(GL_TEXTURE_2D, image_texture);

    // Setup filtering parameters for display
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                    GL_CLAMP_TO_EDGE);                                   // This is required on WebGL for non
                                                                         // power-of-two textures
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); // Same

    // Upload pixels into texture
#if defined(GL_UNPACK_ROW_LENGTH) && !defined(__EMSCRIPTEN__)
    glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
#endif
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image_width, image_height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image_data);
    _m ? free(image_data) : stbi_image_free(image_data);

    *out = image_texture;
    *out_width = image_width;
    *out_height = image_height;

    return true;
}