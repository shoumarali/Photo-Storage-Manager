//
// Created by ashoumar on 3/26/25.
//

#include <jni.h>
#include <chrono>
#include "log_utils.h"
#include "native_root_checker.h"
#include "native_system_integrity_verifier.h"
#include "native_emulator_checker.h"


/**
 * @brief JNI entry point to check if the device is rooted or compromised.
 * @param env The JNI environment pointer.
 * @param thiz The reference to the calling Java object (MainActivity).
 * @return JNI_TRUE if the device is detected to be rooted or compromised, otherwise JNI_FALSE.
 *
 * @details This function performs multiple checks to determine if the device is rooted,
 * modified, or running in an emulator. It combines various native checks for system integrity
 * to provide a more reliable root detection solution. The checks include root detection,
 * system modifications, and emulator detection.
 * The function also measures execution time and logs the result for debugging purposes.
 */
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

    jboolean isRootedNative = isRootedUsingNativeChecks();
    jboolean isSystemModded = isSystemModified();
    jboolean isRunningOnEmulator = isDeviceEmulator();
    jboolean isRooted = isRootedNative || isSystemModded || isRunningOnEmulator;

    auto end_time = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(end_time - start_time).count();
    LOGI("✅ [Root Check] Completed in %lld ms | Status: %s",
         duration,
         isRooted ? "UNSAFE (root detected)" : "SAFE");

    return  isRooted;
}