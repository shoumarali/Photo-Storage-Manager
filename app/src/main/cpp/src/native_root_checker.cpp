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

/**
 * @brief Checks for common superuser binaries
 * @return JNI_TRUE if any known SU binary path exists, JNI_FALSE otherwise
 *
 * @details Scans filesystem for binaries at:
 *          - Standard locations (/system/bin, /system/xbin)
 *          - Common alternative install paths
 *          - Temporary directories
 *          - Hidden paths (/.ext/.su)
 *
 * @note Also checks executable permissions where applicable
 */
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

/**
 * @brief Detects BusyBox installations
 * @return JNI_TRUE if BusyBox binary found, JNI_FALSE otherwise
 *
 * @details Checks common BusyBox locations:
 *          - System binaries directories
 *          - Development paths
 *          - Temporary install locations
 *
 * @note BusyBox alone doesn't guarantee root but often co-exists
 */
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

/**
 * @brief Scans for root management apps
 * @return JNI_TRUE if known root apps detected, JNI_FALSE otherwise
 *
 * @details Checks for:
 *          - Superuser.apk
 *          - SuperSU.apk
 *          - Magisk and its variants
 *          - Both system and data partitions
 */
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

/**
 * @brief Magisk-specific detection
 * @return JNI_TRUE if Magisk artifacts found, JNI_FALSE otherwise
 *
 * @details Looks for:
 *          - Magisk binaries
 *          - Configuration files
 *          - Database artifacts
 *          - Hidden installations
 *
 * @note Magisk can hide these paths - consider runtime checks
 */
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

/**
 * @brief Checks for SU symlinks
 * @return JNI_TRUE if SU is a symlink, JNI_FALSE otherwise
 *
 * @details Verifies SU binaries at:
 *          - /system/xbin/su
 *          - /system/bin/su
 *
 * @note Symlinks may indicate root management systems
 */
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

/**
 * @brief Inspects PATH for SU entries
 * @return JNI_TRUE if PATH contains SU references, JNI_FALSE otherwise
 *
 * @warning Environment variables can be spoofed
 * @note Checks both ':su' and 'su:' patterns
 */
jboolean isSuInEnvironmentPath() {
    const char* path = getenv("PATH");
    if (path != nullptr && (strstr(path, ":su") != nullptr || strstr(path, "su:") != nullptr)) {
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
