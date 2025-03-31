//
// Created by ashoumar on 3/28/25.
//

#include <fstream>
#include <string>
#include <sstream>
#include "native_system_integrity_verifier.h"
#include "log_utils.h"

jboolean doWritableSystemPartitionsExist();
jboolean isBootPartitionModified();
//jboolean isSELinuxEnforcing();

jboolean isSystemModified(){
    return doWritableSystemPartitionsExist() || isBootPartitionModified();
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