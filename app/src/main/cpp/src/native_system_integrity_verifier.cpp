//
// Created by ashoumar on 3/28/25.
//

#include <fstream>
#include <string>
#include <sstream>
#include <unistd.h>
#include "sys/system_properties.h"
#include "native_system_integrity_verifier.h"
#include "log_utils.h"


jboolean doWritableSystemPartitionsExist();
jboolean isBootSecurityCompromised();
jboolean isRunningDebugBuild();
jboolean isSystemDebuggable();

jboolean isSystemModified(){
    return doWritableSystemPartitionsExist() ||
    isBootSecurityCompromised() ||
    isRunningDebugBuild() ||
    isSystemDebuggable();
}


/**
 * Checks if any critical system partitions are mounted as read-write.
 *
 * This function reads the `/proc/mounts` file, which contains information
 * about the mount points and their options. It looks for the following system
 * partitions: "/system", "/vendor", and "/product". If any of these partitions
 * are mounted with read-write ("rw") permissions, it indicates that they are
 * writable and may have been compromised or unlocked.
 *
 * The check considers the following:
 *   - "/system" partition: Typically where the OS and system apps reside.
 *   - "/vendor" partition: Contains vendor-specific system files.
 *   - "/product" partition: Used for product-specific data or customization.
 *
 * If any of these partitions are found to be writable, it suggests that the device
 * may have been tampered with or is in a non-secure state.
 *
 * @return JNI_TRUE if any critical system partition is writable, otherwise JNI_FALSE.
 */

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

/**
 * Checks if the device's boot security has been compromised.
 *
 * This function examines multiple system properties to assess boot integrity:
 *   - "ro.boot.verifiedbootstate": Should be "green" or "yellow". If "orange", the bootloader is unlocked or not verified.
 *   - "ro.boot.flash.locked": Should be "1" (locked). If "0", the bootloader is flash-unlocked.
 *   - "ro.boot.veritymode": Should be "enforcing". Any other value (e.g., "logging", "disabled") indicates dm-verity is off.
 *
 * These properties help detect:
 *   - Unlocked bootloaders
 *   - Custom/recovery images
 *   - Disabled verity checks
 *
 * Note:
 *   - Values can vary slightly across manufacturers and Android versions.
 *   - This function errs on the side of caution and returns JNI_TRUE if any check fails.
 *
 * @return JNI_TRUE if any boot-related integrity check fails, otherwise JNI_FALSE.
 */

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

/**
 * Checks if the system is debuggable or insecure.
 *
 * This function reads two system properties:
 *   - "ro.secure": Indicates whether the system is in secure mode.
 *     - A value of "0" means the system is not secure.
 *   - "ro.debuggable": Indicates whether debugging is enabled.
 *     - A value of "1" means the system is debuggable.
 *
 * The function returns JNI_TRUE if either:
 *   - The system is not secure (ro.secure == "0").
 *   - Debugging is enabled (ro.debuggable == "1").
 *
 * A debuggable system or an insecure system can pose a security risk.
 *
 * @return JNI_TRUE if the system is insecure or debuggable, otherwise JNI_FALSE.
 */


jboolean isSystemDebuggable() {
    char secure[PROP_VALUE_MAX], debuggable[PROP_VALUE_MAX];
    __system_property_get("ro.secure", secure);
    __system_property_get("ro.debuggable", debuggable);

    LOGV("secure %s and debuggable %s", secure, debuggable);
    return (strcmp(secure, "0") == 0 || strcmp(debuggable, "1") == 0) ? JNI_TRUE : JNI_FALSE;
}

/**
 * Determines if the device is running a debug build of Android.
 * This checks the fundamental build type of the operating system.
 *
 * @return JNI_TRUE if build type is either:
 *         - "eng" (engineering build), or
 *         - "userdebug" (debuggable user build)
 *         Returns JNI_FALSE for standard "user" builds
 */

jboolean isRunningDebugBuild() {
    char buildType[PROP_VALUE_MAX];
    __system_property_get("ro.build.type", buildType);

    LOGV("Android build type: %s", buildType);

    if (strcmp(buildType, "eng") == 0 || strcmp(buildType, "userdebug") == 0) {
        LOGE("Debug build detected");
        return JNI_TRUE;
    }

    LOGI("Standard production build detected");
    return JNI_FALSE;
}


/**
 * Checks if SELinux is not enforcing on the device.
 *
 * Attempts to read "/sys/fs/selinux/enforce" to determine the SELinux mode:
 *   - "1" means enforcing (returns JNI_FALSE)
 *   - "0" means permissive or disabled (returns JNI_TRUE)
 *
 * Note: Accessing this path typically requires root/superuser privileges.
 * If the file cannot be accessed or read, the function conservatively returns JNI_TRUE.
 *
 * @return JNI_TRUE if SELinux is not enforcing or the check fails, otherwise JNI_FALSE.
 */
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

/**
 * Attempts to determine SELinux mode by executing the "getenforce" command.
 *
 * This method runs "getenforce" via popen() and inspects the output:
 *   - If the output contains "Enforcing", it returns JNI_FALSE.
 *   - Any other output or failure indicates SELinux is not enforcing (returns JNI_TRUE).
 *
 * Limitations:
 *   - This approach often fails on production Android devices due to sandboxing.
 *   - "getenforce" may not exist, or execution may be blocked.
 *   - Considered unreliable for production use, primarily for debugging or rooted devices.
 *
 * @return JNI_TRUE if SELinux is not enforcing or command execution fails, otherwise JNI_FALSE.
 */

jboolean isSELinuxNotEnforcingUsingGetEnforce() {
    FILE* cmd = popen("getenforce 2>&1", "r");
    if (cmd) {
        char output[16] = {0};
        if (fgets(output, sizeof(output), cmd)) {
            pclose(cmd);

            output[strcspn(output, "\n")] = '\0';
            LOGV("Raw getenforce output: '%s'", output);

            if (strstr(output, "Enforcing")) {
                return JNI_FALSE;
            }
        }
    }
    return JNI_TRUE;
}

/**
 * Checks whether SELinux has been compromised or is not enforcing.
 *
 * This function reads system properties to determine the SELinux status:
 *   - Tries "ro.boot.selinux" first, then "ro.build.selinux" as a fallback.
 *   - Expected value is "enforcing"; anything else indicates a potential compromise.
 *
 * Note:
 *   - Some manufacturers may omit these properties.
 *   - Android does not enforce the existence of SELinux status properties.
 *   - On such devices, this check may return JNI_TRUE due to missing values.
 *
 * @return JNI_TRUE if SELinux is not enforcing or the value is missing; otherwise JNI_FALSE.
 */

jboolean isSELinuxCompromised() {
    char selinux[PROP_VALUE_MAX] = {0};

    if (__system_property_get("ro.boot.selinux", selinux) <= 0) {
        __system_property_get("ro.build.selinux", selinux);
    }

    LOGV("Raw SELinux property value: '%s'", selinux);

    if (strcmp(selinux, "enforcing") != 0) {
        LOGE("SELinux compromised (%s)", selinux);
        return JNI_TRUE;
    }
    return JNI_FALSE;
}
