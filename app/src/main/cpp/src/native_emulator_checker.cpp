//
// Created by ashoumar on 4/5/25.
//

#include "native_emulator_checker.h"
#include <unistd.h>
#include <cstring>
#include "android/sensor.h"
#include  "sys/system_properties.h"
#include "log_utils.h"

jboolean emulatorFileExists();
jboolean checkBuildProperties();
jboolean checkEmulatorProperties();
jboolean checkCPUArchitecture();

jboolean isDeviceEmulator(){
    return checkCPUArchitecture() ||
    emulatorFileExists() ||
    checkBuildProperties() ||
    checkEmulatorProperties();
}


/**
 * @brief Checks CPU architecture for emulator indicators
 * @return JNI_TRUE if x86 or x86_64 architecture is detected (common for emulators)
 *
 * @details Real Android devices typically use ARM architectures (armeabi, arm64-v8a),
 * while emulators often run on x86/x86_64 CPUs.
 *
 * @note Some rare Android devices do use Intel processors, which may cause false positives.
 */
jboolean checkCPUArchitecture() {
    char cpu_abi[PROP_VALUE_MAX];

    __system_property_get("ro.product.cpu.abi", cpu_abi);

    LOGV("cpu1 %s", cpu_abi);

    return (strstr(cpu_abi, "x86") != NULL) ||
           (strstr(cpu_abi, "x86_64") != NULL);
}

/**
 * @brief Checks for QEMU-specific system properties
 * @return JNI_TRUE if the QEMU emulator property is detected (ro.kernel.qemu == 1)
 *
 * @details QEMU-based emulators (like the standard Android emulator) set this property.
 * This is one of the most reliable indicators of emulator environment.
 */
jboolean checkEmulatorProperties() {
    char prop[PROP_VALUE_MAX];
    __system_property_get("ro.kernel.qemu", prop);
    return strcmp(prop, "1") == 0;
}

/**
 * @brief Checks build properties for emulator indicators
 * @return JNI_TRUE if known emulator patterns are found in model or manufacturer strings
 *
 * @details Checks for:
 * - "Emulator" in model name
 * - "Android SDK built for x86" in model name
 * - "Genymotion" in manufacturer name (popular alternative emulator)
 */
jboolean checkBuildProperties() {
    char model[PROP_VALUE_MAX];
    char manufacturer[PROP_VALUE_MAX];

    __system_property_get("ro.product.model", model);
    __system_property_get("ro.product.manufacturer", manufacturer);

    return strstr(model, "Emulator") != nullptr ||
           strstr(model, "Android SDK built for x86") != nullptr ||
           strstr(manufacturer, "Genymotion") != nullptr;
}

/**
 * @brief Checks for existence of emulator-specific files
 * @return JNI_TRUE if any known emulator file is found
 *
 * @details Checks for these files which are typically present in emulator environments:
 * - /dev/socket/qemud        : QEMU daemon socket
 * - /dev/qemu_pipe          : QEMU pipe
 * - /system/lib/libc_malloc_debug_qemu.so : QEMU debug library
 * - /sys/qemu_trace         : QEMU trace file
 *
 * @note Uses access() with F_OK to check file existence without opening
 */
jboolean emulatorFileExists() {
    const char* emulatorFiles[] = {
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace"
    };

    for (auto & emulatorFile : emulatorFiles) {
        if (access(emulatorFile, F_OK) == 0) {
            LOGV("Emulator file detected: %s", emulatorFile);
            return JNI_TRUE;
        }
    }

    LOGV(" No known emulator files detected.");
    return JNI_FALSE;
}