//
// Created by ashoumar on 3/27/25.
//

#ifndef ANDROIDSTORAGE_LOG_UTILS_H
#define ANDROIDSTORAGE_LOG_UTILS_H

#include <android/log.h>

#ifndef LOG_TAG
#define LOG_TAG "ROOT_CHECK"
#endif

#ifdef NDEBUG
#define LOGV(...) ((void)0)
#else
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#endif

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

#define LOGF(...) do { \
  __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, __VA_ARGS__); \
  abort(); \
} while (0)

#endif
