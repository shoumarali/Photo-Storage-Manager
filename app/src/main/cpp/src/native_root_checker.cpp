//
// Created by ashoumar on 3/28/25.
//

#include "native_root_checker.h"
#include <cstdlib>
#include <cstring>
#include <unistd.h>
#include <sys/stat.h>
#include <cstdio>
#include "log_utils.h"

jboolean doSuperUserBinariesExist();
jboolean doBusyBoxBinariesExist();
jboolean doSuperUserAppsExist();
jboolean isRootedUsingMagisk();
jboolean isSuASymlink();
jboolean isSuInEnvironmentPath();

jboolean isRootedUsingNativeChecks() {

    if (doSuperUserBinariesExist()) {
        LOGE("Root check: Superuser binaries exist!");
        return JNI_TRUE;
    }

    if (doBusyBoxBinariesExist()) {
        LOGE("Root check: BusyBox binaries exist!");
        return JNI_TRUE;
    }

    if (doSuperUserAppsExist()) {
        LOGE("Root check: Superuser apps exist!");
        return JNI_TRUE;
    }

    if (isRootedUsingMagisk()) {
        LOGE("Root check: Magisk is installed!");
        return JNI_TRUE;
    }

    if (isSuASymlink()) {
        LOGE("Root check: 'su' is a symlink!");
        return JNI_TRUE;
    }

    if (isSuInEnvironmentPath()) {
        LOGE("Root check: 'su' found in environment PATH!");
        return JNI_TRUE;
    }

    return JNI_FALSE;
}

jboolean doSuperUserBinariesExist() {
    const char* paths[] = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/system/usr/we-need-root",
            "/cache/su",
            "/data/su",
            "/dev/su",
            "/system/bin/.ext/.su",
            "/system/xbin/.ext/.su",
    };

    for (const char* path : paths) {
        if (access(path, F_OK) == 0 || access(path, X_OK) == 0) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

jboolean doBusyBoxBinariesExist() {
    const char* busybox_binaries[] = {
            "/system/xbin/busybox",
            "/system/bin/busybox",
            "/sbin/busybox",
            "/system/sd/xbin/busybox",
            "/data/local/xbin/busybox",
            "/data/local/bin/busybox",
            "/data/local/busybox",
            "/dev/busybox"
    };

    for (const char* path : busybox_binaries) {
        if (access(path, F_OK) == 0) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

jboolean doSuperUserAppsExist() {
    const char* su_apps[] = {
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/system/app/Magisk.apk",
            "/system/app/MagiskManager.apk",
            "/data/app/eu.chainfire.supersu-1/base.apk",
            "/data/app/eu.chainfire.supersu-2/base.apk",
            "/data/app/com.topjohnwu.magisk-1/base.apk",
            "/data/app/com.topjohnwu.magisk-2/base.apk",
    };

    for (const char* path : su_apps) {
        if (access(path, F_OK) == 0) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

jboolean isRootedUsingMagisk() {
    const char* magisk_files[] = {
            "/sbin/magisk",
            "/cache/magisk.log",
            "/data/adb/magisk",
            "/data/adb/magisk.img",
            "/data/adb/magisk.db",
            "/system/bin/.ext/.magisk",
            "/system/etc/init.d/99SuperSUDaemon",
            "/system/bin/daemonsu",
            "/system/xbin/daemonsu"
    };

    for (const char* path : magisk_files) {
        if (access(path, F_OK) == 0) {
            return JNI_TRUE;
        }
    }

    return JNI_FALSE;
}

jboolean isSuASymlink() {
    struct stat fileStat{};
    if (lstat("/system/xbin/su", &fileStat) == 0 && S_ISLNK(fileStat.st_mode)) {
        return JNI_TRUE;
    }
    if (lstat("/system/bin/su", &fileStat) == 0 && S_ISLNK(fileStat.st_mode)) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

jboolean isSuInEnvironmentPath() {
    const char* path = getenv("PATH");
    if (path != nullptr && (strstr(path, ":su") != nullptr || strstr(path, "su:") != nullptr)) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
