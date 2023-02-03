//
// Created by askan on 30/01/23.
//
#pragma once

#include <GLES3/gl3.h>
#include <android/asset_manager.h>
#include <jni.h>
#include <EGL/egl.h>
#include <android/native_window_jni.h>
#include <android/asset_manager_jni.h>
#include <imgui.h>
#include <imgui_impl_android.h>
#include <imgui_impl_opengl3.h>
#include <strings.h>
#include <android/log.h>
#include "fonts/casncadia_mono.h"
#include "imgui_internal.h"
#include <dobby.h>
#include <vector>
#include "ImGuiSurface.h"
#include "AssetsHelper.hpp"

bool g_Initialized, g_WindowRendered;
AAssetManager *g_AssetsManager;
ANativeWindow *g_NativeWindow;
int screenWidth, screenHeight;

std::vector<const char *> window_names;
// call this when you make new window
bool addWindowName(const char *name) {
    for (auto &n: window_names) {
        if (n == name) {
            return false;
        }
    }
    window_names.push_back(name);
}

extern "C"
JNIEXPORT void JNICALL
Java_akn_main_ImGuiSurface_Init(JNIEnv *env, jclass clazz,
                                 jobject asset_mgr, jobject surface) {
    if (g_Initialized) return;


    g_AssetsManager = AAssetManager_fromJava(env, asset_mgr);
    g_NativeWindow = ANativeWindow_fromSurface(env, surface);

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO &io = ImGui::GetIO();
    io.ConfigFlags |= ImGuiConfigFlags_IsTouchScreen;
    io.IniFilename = nullptr;

    ImGui::StyleColorsLight();
    ImGui_ImplAndroid_Init(g_NativeWindow);
    ImGui_ImplOpenGL3_Init("#version 300 es");

    ImFontConfig font_cfg;
    font_cfg.SizePixels = 28.0f;
    io.Fonts->AddFontFromMemoryCompressedBase85TTF(CascadiaMono_compressed_data_base85, 28.0f, &font_cfg);
    
    ImGuiStyle& style = ImGui::GetStyle();
    style.ScaleAllSizes(3.0f);
    style.ScrollbarSize = 40.0f;
    style.WindowRounding = 5.0f;
    style.ScrollbarRounding = 5.0f;
    style.FrameRounding = 5.0f;
    style.GrabRounding = 5.0f; 

    g_Initialized = true;

}
extern "C"
JNIEXPORT void JNICALL
Java_akn_main_ImGuiSurface_SurfaceChanged(JNIEnv *env, jclass clazz, jobject gl, jint width,
                                            jint height) {
    screenWidth = width;
    screenHeight = height;

    glViewport(0, 0, width, height);
    ImGuiIO &io = ImGui::GetIO();
    io.DisplaySize = ImVec2((float)width, (float)height);
}
extern "C"
JNIEXPORT void JNICALL
Java_akn_main_ImGuiSurface_Tick(JNIEnv *env, jclass clazz, jobject thiz) {
    if (!g_Initialized) return;

    ImGui_ImplOpenGL3_NewFrame();
    ImGui_ImplAndroid_NewFrame(screenWidth, screenHeight);
    ImGui::NewFrame();

    static ImGuiWindowFlags windowFlags = ImGuiWindowFlags_AlwaysAutoResize;

    ImGui::Begin("ssss", nullptr, windowFlags);
    addWindowName("ssss");
    ImGui::Button("Button");
    static bool a;
    ImGui::Checkbox("New Window", &a);
    ImGui::End();

    if (a) {
        ImGui::Begin("wwwww", &a, windowFlags);
        addWindowName("wwwww");
        ImGui::Button("Button");
        ImGui::End();

        ImGui::ShowDemoWindow(&a);
        addWindowName("Dear ImGui Demo");
    }

    ImGui::Render();
    glViewport(0, 0, screenWidth, screenHeight);
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

    g_WindowRendered = true;
}


extern "C"
JNIEXPORT void JNICALL
Java_akn_main_ImGuiSurface_Shutdown(JNIEnv *env, jclass clazz) {
    if (!g_Initialized)
        return;

    g_Initialized=false;

    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplAndroid_Shutdown();
    ImGui::DestroyContext();
    ANativeWindow_release(g_NativeWindow);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_akn_main_ImGuiSurface_Initialized(JNIEnv *env, jclass clazz) {
    return g_Initialized && g_WindowRendered;
}

extern "C"
JNIEXPORT void JNICALL
Java_akn_main_ImGuiSurface_MotionEvent(JNIEnv *env, jclass clazz, jobject event) {
    if (!g_Initialized) return;

    jclass cMotionEvent = env->GetObjectClass(event);
    jmethodID getX = env->GetMethodID(cMotionEvent, "getX", "()F");
    jmethodID getY = env->GetMethodID(cMotionEvent,"getY", "()F");
    jmethodID getAction = env->GetMethodID(cMotionEvent, "getAction", "()I");

    auto x = env->CallFloatMethod(event, getX);
    auto y = env->CallFloatMethod(event, getY);
    auto action = env->CallIntMethod(event, getAction);

    __android_log_print(ANDROID_LOG_INFO, "GPX", "AC: %i X %.2f Y %.2f", action,x,y);

    ImGui_ImplAndroid_HandleInputEvent((int)x,(int)y,action);
}
extern "C"
JNIEXPORT jobjectArray JNICALL
Java_akn_main_ImGuiSurface_GetWindowsTracked(JNIEnv *env, jclass clazz) {
    if (!g_Initialized || !g_WindowRendered) {
        return env->NewObjectArray(0, env->FindClass("java/lang/String"),env->NewStringUTF(""));
    }
    auto len = window_names.size();
    static char res[512];
    jobjectArray rets = env->NewObjectArray((int)len, env->FindClass("java/lang/String"), env->NewStringUTF(""));

    for (int i = 0; i < window_names.size(); i++) {
        ImGuiWindow *window = ImGui::FindWindowByName(window_names[i]);
        if (window == nullptr || !window->Active) continue;

        ImVec2 &pos = window->Pos;
        ImVec2 &size = window->Size;

        if (pos.x < 0) pos.x = 0;
        if (pos.y < 0) pos.y = 0;
        if (pos.x + size.x > (float)screenWidth) pos.x = (float)screenWidth - size.x;
        if (pos.y + size.y > (float)screenHeight) pos.y = (float)screenHeight - size.y;

        memset(res, '\0', 512);
        sprintf(res, "%d|%.4f|%.4f|%.4f|%.4f", (int)window->ID, pos.x, pos.y, size.x, size.y);
        env->SetObjectArrayElement(rets, i, env->NewStringUTF(res));
    }
    return rets;
}
extern "C"
JNIEXPORT void JNICALL
Java_akn_main_ImGuiSurface_OnTouch(JNIEnv *env, jclass clazz, jboolean down, jfloat x, jfloat y) {
    ImGuiIO & io = ImGui::GetIO();
    io.MouseDown[0] = down;
    io.MousePos = ImVec2(x,y);
}