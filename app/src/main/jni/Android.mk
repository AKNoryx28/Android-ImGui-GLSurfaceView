LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE            := libcurl
LOCAL_SRC_FILES         := libs/$(TARGET_ARCH_ABI)/libcurl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE            := libcrypto
LOCAL_SRC_FILES         := libs/$(TARGET_ARCH_ABI)/libcrypto.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE            := libssl
LOCAL_SRC_FILES         := libs/$(TARGET_ARCH_ABI)/libssl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE            := libdobby
LOCAL_SRC_FILES         := libs/$(TARGET_ARCH_ABI)/libdobby.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := GPX
LOCAL_ARM_MODE := arm

LOCAL_CFLAGS 	:= -Wno-error=format-security -fvisibility=hidden -ffunction-sections
LOCAL_CFLAGS 	+= -fno-rtti -fno-exceptions -fpermissive -fdata-sections -w
LOCAL_CPPFLAGS 	:= -Wno-error=format-security -fpermissive -fvisibility=hidden
LOCAL_CPPFLAGS 	+= -fno-rtti -fno-exceptions -fms-extensions -Wno-error=c++11-narrowing
LOCAL_CPPFLAGS 	+= -ffunction-sections -fdata-sections  -w -Werror -s -std=c++17

LOCAL_LDLIBS 	:= -llog -landroid -lEGL -lGLESv3 -lz

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/src/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/imgui
LOCAL_C_INCLUDES += $(LOCAL_PATH)/imgui/backends

LOCAL_SRC_FILES := src/ImGuiSurface.cpp \
				imgui/imgui_draw.cpp imgui/imgui_tables.cpp \
				imgui/imgui_widgets.cpp imgui/imgui.cpp imgui/imgui_demo.cpp \
				imgui/backends/imgui_impl_android.cpp \
				imgui/backends/imgui_impl_opengl3.cpp \

LOCAL_STATIC_LIBRARIES := libdobby libcurl libcrypto libssl

include $(BUILD_SHARED_LIBRARY)