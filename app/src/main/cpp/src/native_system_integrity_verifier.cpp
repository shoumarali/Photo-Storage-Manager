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
 * @brief Verifies if protected system partitions are mounted with read-write access.
 * @return JNI_TRUE if any critical system partition is writable, otherwise JNI_FALSE.
 * @details Scans `/proc/mounts` to detect writable states of critical Android partitions:
 *          - /system (core OS and privileged apps)
 *          - /vendor (hardware-specific binaries/configs)
 *          - /product (OEM customizations)
 *          Production devices should mount these partitions read-only (ro) after boot.
 *          Write access (rw) typically indicates:
 *          - Active root access (remount performed)
 *          - Custom ROM/recovery modification
 *          - Development/test device configuration
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
 * @brief Checks if the device's boot security has been compromised.
 * @return JNI_TRUE if any boot-related integrity check fails, otherwise JNI_FALSE.
 * @details This function parses `/proc/mounts` to detect if any of the following
 *          system partitions are mounted with 'rw' (read-write) flags:
 *          - "/system" (core OS and system applications)
 *          - "/vendor" (vendor-specific binaries and configurations)
 *          - "/product" (product-specific customizations)
 *          On secure, unmodified devices, these partitions should typically be
 *          mounted read-only ('ro') after initial boot. Finding them writable may indicate:
 *          - The device is rooted or bootloader-unlocked
 *          - The system has been remounted for modification
 *          - The device is running in a development/debug configuration
 *
 * @warning Values can vary slightly across manufacturers and Android versions.
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
 * @brief Checks if the system is debuggable or insecure.
 * @return JNI_TRUE if the system is insecure or debuggable, otherwise JNI_FALSE.
 * @details Checks two critical security properties:
 *          - @c ro.secure : System security flag
 *            - "0" : Runs as root, no security restrictions (insecure)
 *            - "1" : Normal secure mode (default on production devices)
 *          - @c ro.debuggable : Debug capability flag
 *            - "0" : Debugging disabled (production default)
 *            - "1" : Allows debugging and additional privileges
 * @note Security implications:
 *       - Insecure mode (ro.secure=0) gives root access to ADB
 *       - Debuggable mode (ro.debuggable=1) enables:
 *         - ADB root commands
 *         - Memory inspection
 *         - Additional logging
 *       - On production devices, both should normally be disabled.
 */


jboolean isSystemDebuggable() {
    char secure[PROP_VALUE_MAX], debuggable[PROP_VALUE_MAX];
    __system_property_get("ro.secure", secure);
    __system_property_get("ro.debuggable", debuggable);

    LOGV("secure %s and debuggable %s", secure, debuggable);
    return (strcmp(secure, "0") == 0 || strcmp(debuggable, "1") == 0) ? JNI_TRUE : JNI_FALSE;
}

/**
 * @brief Determines if the device is running a debug build of Android.
 * * @return JNI_TRUE if build type is either @c "eng" (engineering build),
 *         or @c "userdebug" (debuggable user build)
 *         Returns JNI_FALSE for standard "user" builds
 *
 *
 * @details Checks the 'ro.build.type' system property which represents the
 *          compilation type of the Android OS.
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
 * @brief Checks if SELinux is not enforcing on the device.
 * @return JNI_TRUE if SELinux is not enforcing or the check fails, otherwise JNI_FALSE.
 * @details Attempts to read "/sys/fs/selinux/enforce" to determine the SELinux mode:
 *   - "1" means enforcing (returns JNI_FALSE)
 *   - "0" means permissive or disabled (returns JNI_TRUE)
 *
 * @warning Accessing this path typically requires root/superuser privileges.
 * If the file cannot be accessed or read, the function conservatively returns JNI_TRUE.
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
 * @brief Attempts to determine SELinux mode by executing the "getenforce" command.
 * @return JNI_TRUE if SELinux is not enforcing or command execution fails, otherwise JNI_FALSE.
 * @details This method runs "getenforce" via popen() and inspects the output:
 *   - If the output contains @c Enforcing , it returns JNI_FALSE.
 *   - Any other output or failure indicates SELinux is not enforcing (returns JNI_TRUE).
 * @warning Limitations:
 *   - This approach often fails on production Android devices due to sandboxing.
 *   - "getenforce" may not exist, or execution may be blocked.
 *   - Considered unreliable for production use, primarily for debugging or rooted devices.
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
 * @brief Checks whether SELinux has been compromised or is not enforcing.
 * @return JNI_TRUE if SELinux is not enforcing or the value is missing; otherwise JNI_FALSE.
 * @details This function reads system properties to determine the SELinux status:
 *   - Tries "ro.boot.selinux" first, then "ro.build.selinux" as a fallback.
 *   - Expected value is "enforcing"; anything else indicates a potential compromise.
 *
 * @warning
 *   Some manufacturers may omit these properties.
 *   - Android does not enforce the existence of SELinux status properties.
 *   - On such devices, this check may return JNI_TRUE due to missing values.
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
