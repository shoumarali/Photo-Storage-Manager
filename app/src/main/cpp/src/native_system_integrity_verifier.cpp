//
// Created by ashoumar on 3/28/25.
//

#include <fstream>
#include <string>
#include <sstream>
#include "sys/system_properties.h"
#include "native_system_integrity_verifier.h"
#include "log_utils.h"
#include <unistd.h>




jboolean doWritableSystemPartitionsExist();
jboolean isBootSecurityCompromised();
jboolean isDebuggableBuild();
jboolean isDeveloperModeEnabled();

jboolean isSystemModified(){
    return doWritableSystemPartitionsExist() || isBootSecurityCompromised();
}

jboolean doWritableSystemPartitionsExist(){
    std::ifstream mountFile("/proc/mounts");

    if(!mountFile.is_open()){
        LOGE("Failed to open /proc/mounts: %s", strerror(errno));
        return JNI_FALSE;
    }
    std::string line;
    while (std::getline(mountFile,line)){
        std::istringstream stream(line);
        std::string device, mountPoint, fileSystem, options;

        stream >> device >> mountPoint >> fileSystem >> options;

        if(mountPoint == "/system" || mountPoint == "/vendor" || mountPoint == "/product"){
            if(options.find("rw") != std::string::npos){
                return JNI_TRUE;
            }
        }
    }
    return JNI_FALSE;
}

jboolean isBootSecurityCompromised() {
    char vbmeta[PROP_VALUE_MAX] = {0};
    if (__system_property_get("ro.boot.verifiedbootstate", vbmeta) > 0) {
        if (strcmp(vbmeta, "orange") == 0) {
            LOGV("Bootloader unlocked/modified (orange state)");
            return JNI_TRUE;
        }
    }

    char flash_locked[PROP_VALUE_MAX] = {0};
    if (__system_property_get("ro.boot.flash.locked", flash_locked) > 0) {
        if (strcmp(flash_locked, "0") == 0) {
            LOGV("Bootloader flash unlocked (modified risk)");
            return JNI_TRUE;
        }
    }

    char verity_mode[PROP_VALUE_MAX] = {0};
    if (__system_property_get("ro.boot.veritymode", verity_mode) > 0) {
        if (strcmp(verity_mode, "enforcing") != 0) {
            LOGV("dm-verity disabled (modified boot/recovery)");
            return JNI_TRUE;
        }
    }

    return JNI_FALSE;
}

jboolean isDeveloperModeEnabled() {
    // Check if the adb binary is accessible
    if (access("/system/bin/adb", F_OK) == 0) {
        LOGE("adb is accessible.");
        return JNI_TRUE; // Developer mode is likely enabled
    } else {
        LOGE("adb is not accessible.");
        return JNI_FALSE; // Developer mode is not enabled
    }
}



jboolean isDebuggableBuild() {
    char buildType[PROP_VALUE_MAX];
    __system_property_get("ro.build.type", buildType);

    // Log the build type to see what it actually is
    LOGE("Build Type: %s", buildType);

    // Check if the build type is 'eng' or 'userdebug'
    if (strcmp(buildType, "eng") == 0 || strcmp(buildType, "userdebug") == 0) {
        LOGE("Device is running a debuggable build.");
        return JNI_TRUE;
    } else {
        LOGE("Device is not running a debuggable build.");
        return JNI_FALSE;
    }
}


// this will not work accessing this path requires super user
jboolean isSELinuxNotEnforcing() {
    const char* path = "/sys/fs/selinux/enforce";

    if (access(path, F_OK | R_OK) != 0) {
        LOGE("SELinux enforce file not accessible: %s", strerror(errno));
        return JNI_TRUE;
    }

    std::ifstream selinux(path);
    if (!selinux.is_open()) {
        LOGE("Failed to open SELinux enforce file: %s", strerror(errno));
        return JNI_TRUE;
    }

    std::string mode;
    if (!(selinux >> mode)) {
        LOGE("Failed to read SELinux enforce mode");
        return JNI_TRUE;
    }

    LOGE("SELinux mode: %s", mode.c_str());
    return (mode == "1") ? JNI_FALSE : JNI_TRUE;
}

//this will not work the process will always throws an error
jboolean isSELinuxNotEnforcingUsingGetEnforce() {
    FILE* cmd = popen("getenforce 2>&1", "r");
    if (cmd) {
        char output[16] = {0};
        if (fgets(output, sizeof(output), cmd)) {
            pclose(cmd);

            output[strcspn(output, "\n")] = '\0';
            LOGV("Raw getenforce output: '%s'", output);

            if (strstr(output, "Enforcing")) {
                LOGV("SELinux is enforcing (safe)");
                return JNI_FALSE;
            }
        }
    }
    return JNI_TRUE;
}
/* in selinux 0 means Permissive,  1 enforcing, -1 disabled
 * it must be enforcing to block unauthorized actions.
 * in the code below selinux will always be empty because some manufacturers
 * don’t expose these properties and android doesn’t enforce these properties to exist.
 */
jboolean isSELinuxCompromised() {
    char selinux[PROP_VALUE_MAX] = {0};

    if (__system_property_get("ro.boot.selinux", selinux) <= 0) {
        __system_property_get("ro.build.selinux", selinux);
    }

    LOGV("Raw SELinux property value: '%s'", selinux);

    if (strcmp(selinux, "enforcing") != 0) {
        LOGV("SELinux compromised (%s)", selinux);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
