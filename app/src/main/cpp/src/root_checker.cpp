//
// Created by ashoumar on 3/26/25.
//

#include <jni.h>
#include "log_utils.h"
#include "root_beer_checker.h"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_alishoumar_androidstorage_MainActivity_isDeviceRootedNative(JNIEnv *env, jobject thiz){

    jclass activityClass = env->GetObjectClass(thiz);
    if (activityClass == nullptr) {
        LOGE("Failed to find MainActivity class");
        return JNI_FALSE;
    }

    jmethodID getApplicationContextMethod = env->GetMethodID(
            activityClass,
            "getApplicationContext",
            "()Landroid/content/Context;");
    if (getApplicationContextMethod == nullptr) {
        LOGE("Failed to find getApplicationContext() method");
        env->DeleteLocalRef(activityClass);
        return JNI_FALSE;
    }

    jobject context = env->CallObjectMethod(thiz, getApplicationContextMethod);
    env->DeleteLocalRef(activityClass);
    if (context == nullptr) {
        LOGE("Failed to obtain application context");
        return JNI_FALSE;
    }

    jboolean isRooted = isRootedUsingRootBeerChecker(env, context);

    return isRooted;
}