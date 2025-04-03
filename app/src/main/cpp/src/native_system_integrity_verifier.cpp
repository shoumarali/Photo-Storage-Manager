//
// Created by ashoumar on 3/28/25.
//

#include <fstream>
#include <string>
#include <sstream>
#include "sys/system_properties.h"
#include "native_system_integrity_verifier.h"
#include "log_utils.h"




jboolean doWritableSystemPartitionsExist();
jboolean isBootPartitionModified();
jboolean isDebuggableBuild();
jboolean isDeveloperModeEnabled();
//jboolean isSELinuxEnforcing();

jboolean isSystemModified(){
    return isDeveloperModeEnabled();
}

jboolean doWritableSystemPartitionsExist(){
    std::ifstream mountFile("/proc/mounts");

    if(!mountFile.is_open()){
        LOGE("Failed to mount file");
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

jboolean isBootPartitionModified() {
    std::ifstream mountFile("/proc/mounts");
    std::string line;

    while (std::getline(mountFile, line)) {
        std::istringstream stream(line);
        std::string device, mountPoint, fileSystem, options;

        stream >> device >> mountPoint >> fileSystem >> options;
        if (mountPoint == "/boot" && options.find("rw") != std::string::npos) {
            return JNI_TRUE;
        }
    }
    return JNI_FALSE;
}

#include <unistd.h>

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



//jboolean isSELinuxEnforcing() {
//    const char* path = "/sys/fs/selinux/enforce";
//
//    if (access(path, F_OK | R_OK) != 0) {
//        LOGE("SELinux enforce file not accessible: %s", strerror(errno));
//        return JNI_TRUE;
//    }
//
//    std::ifstream selinux(path);
//    if (!selinux.is_open()) {
//        LOGE("Failed to open SELinux enforce file: %s", strerror(errno));
//        return JNI_TRUE;
//    }
//
//    std::string mode;
//    if (!(selinux >> mode)) {
//        LOGE("Failed to read SELinux enforce mode");
//        return JNI_TRUE;
//    }
//
//    LOGE("SELinux mode: %s", mode.c_str());
//    return (mode == "1") ? JNI_FALSE : JNI_TRUE;
//}