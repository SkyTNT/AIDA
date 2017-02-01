LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CPP_EXTENSION := .cpp .cc
LOCAL_MODULE    := aida
LOCAL_SRC_FILES := main.cpp
LOCAL_CPPFLAGS += -fexceptions
LOCAL_LDLIBS    := -L$(LOCAL_PATH) -ldl

include $(BUILD_SHARED_LIBRARY)
