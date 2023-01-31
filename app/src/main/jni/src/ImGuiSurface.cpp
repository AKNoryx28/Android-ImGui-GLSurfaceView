//
// Created by askan on 30/01/23.
//
#pragma once

#include <GLES3/gl3.h>
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

bool g_Initialized, demo;
jobject g_AssetsManager;
ANativeWindow *g_NativeWindow;
ImGuiWindow *g_CurrentWindow;
int screenWidth, screenHeight;

extern "C"
JNIEXPORT void JNICALL
Java_akn_main_ImGuiSurface_Init(JNIEnv *env, jclass clazz,
                                 jobject asset_mgr, jobject surface) {
    if (g_Initialized) return;

    g_AssetsManager = asset_mgr;
    g_NativeWindow = ANativeWindow_fromSurface(env, surface);

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO &io = ImGui::GetIO();
    io.ConfigFlags |= ImGuiConfigFlags_IsTouchScreen;
    io.IniFilename = NULL;

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
Java_akn_main_ImGuiSurface_Tick(JNIEnv *env, jclass clazz) {
    if (!g_Initialized) return;

    ImGuiIO &io = ImGui::GetIO();

    ImGui_ImplOpenGL3_NewFrame();
    ImGui_ImplAndroid_NewFrame(screenWidth, screenHeight);
    ImGui::NewFrame();


    ImGui::Begin("Window");
    ImGui::Text("FPS %.2f", io.Framerate);
    g_CurrentWindow = ImGui::GetCurrentWindow();
    ImGui::End();


    ImGui::Render();
    glViewport(0, 0, screenWidth, screenHeight);
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
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
JNIEXPORT void JNICALL
Java_akn_main_ImGuiSurface_MotionEventClick(JNIEnv *env, jclass clazz, jboolean down,
                                              jfloat pos_x, jfloat pos_y) {
    if (!g_Initialized) return;
    ImGuiIO & io = ImGui::GetIO();
    io.MouseDown[0] = down;
    io.MousePos = ImVec2(pos_x,pos_y);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_akn_main_ImGuiSurface_GetWindowCurrent(JNIEnv *env, jclass clazz) {
    char result[256] = "0|0|0|0";
    if(g_CurrentWindow){
        sprintf(result,"%d|%d|%d|%d",(int)g_CurrentWindow->Pos.x,(int)g_CurrentWindow->Pos.y,(int)g_CurrentWindow->Size.x,(int)g_CurrentWindow->Size.y);
    }
    return env->NewStringUTF(result);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_akn_main_ImGuiSurface_Initialized(JNIEnv *env, jclass clazz) {
    return g_Initialized;
}
