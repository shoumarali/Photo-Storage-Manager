//
// Created by ashoumar on 3/27/25.
//

#ifndef ANDROIDSTORAGE_LOG_UTILS_H
#define ANDROIDSTORAGE_LOG_UTILS_H

#include <android/log.h>

#define LOG_TAG "ROOT_CHECK"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif
