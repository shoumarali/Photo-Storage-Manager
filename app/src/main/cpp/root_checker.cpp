//
// Created by ashoumar on 3/26/25.
//

#include <jni.h>
#include <android/log.h>

extern "C" JNIEXPORT jboolean JNICALL
Java_com_alishoumar_androidstorage_MainActivity_isDeviceRootedNative(JNIEnv *env, jobject thiz){

    jclass rootBeerClass = env->FindClass("com/scottyab/rootbeer/RootBeer");
    if(rootBeerClass == nullptr){
        __android_log_print(ANDROID_LOG_ERROR,"ROOT_CHECK","Root beer clas was not found");
        return false;
    }

    jmethodID constructor = env->GetMethodID(
            rootBeerClass,
            "<init>",
            "(Landroid/content/Context;)V");

    jmethodID isRootedMethod = env->GetMethodID(rootBeerClass, "isRooted", "()Z");

    jobject rootBeerInstance = env->NewObject(rootBeerClass,constructor,thiz);

    jboolean isRooted= env->CallBooleanMethod(rootBeerInstance,isRootedMethod);

    return isRooted;
}