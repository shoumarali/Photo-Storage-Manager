//
// Created by ashoumar on 3/26/25.
//

#include <jni.h>
#include <chrono>
#include "log_utils.h"
#include "root_beer_checker.h"
#include "native_root_checker.h"
#include "native_system_integrity_verifier.h"
#include "native_emulator_checker.h"

extern "C" JNIEXPORT jboolean JNICALL
Java_com_alishoumar_androidstorage_MainActivity_isDeviceRootedNative(JNIEnv *env, jobject thiz){

    auto start_time = std::chrono::high_resolution_clock::now();
    LOGI("🚀 [Root Check] Started");

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

    jboolean isRootedUsingRootBeer = isRootedUsingRootBeerChecker(env, context);
    jboolean isRootedNative = isRootedUsingNativeChecks();
    jboolean isSystemModded = isSystemModified();
    jboolean isRunningOnEmulator = isDeviceEmulator();
    jboolean isRooted = isRootedUsingRootBeer || isRootedNative || isSystemModded || isRunningOnEmulator;

    auto end_time = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time).count();
    LOGI("✅ [Root Check] Completed in %lld ms | Status: %s",
         duration,
         isRooted ? "UNSAFE (root detected)" : "SAFE");

    return  isRooted;
}