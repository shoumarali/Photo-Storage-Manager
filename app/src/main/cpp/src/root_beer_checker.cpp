//
// Created by ashoumar on 3/27/25.
//

#include "root_beer_checker.h"
#include "log_utils.h"

jboolean isRootedUsingRootBeerChecker(JNIEnv *env, jobject context){

    jclass rootBeerClass = env->FindClass("com/scottyab/rootbeer/RootBeer");
    if (rootBeerClass == nullptr) {
        LOGE("RootBeer class was not found");
        return JNI_FALSE;
    }

    jmethodID constructor = env->GetMethodID(
            rootBeerClass,
            "<init>",
            "(Landroid/content/Context;)V");
    if (constructor == nullptr) {
        LOGE("Failed to find RootBeer constructor");
        env->DeleteLocalRef(rootBeerClass);
        return JNI_FALSE;
    }

    jmethodID isRootedMethod = env->GetMethodID(rootBeerClass, "isRooted", "()Z");
    if (isRootedMethod == nullptr) {
        LOGE("Failed to find isRooted method");
        env->DeleteLocalRef(rootBeerClass);
        return JNI_FALSE;
    }

    jobject rootBeerInstance = env->NewObject(rootBeerClass, constructor, context);
    if (rootBeerInstance == nullptr) {
        LOGE("Failed to create RootBeer instance");
        env->DeleteLocalRef(rootBeerClass);
        return JNI_FALSE;
    }

    jboolean isRooted = env->CallBooleanMethod(rootBeerInstance, isRootedMethod);

    env->DeleteLocalRef(rootBeerClass);
    env->DeleteLocalRef(rootBeerInstance);

    return isRooted;
}