# VaultPic: A Privacy-Focused Photo Management Solution
### [View on GitHub →](https://github.com/shoumarali/AndroidStorage)

## Summary
VaultPic is a secure photo vault that protects your private pictures from unauthorized access.
It locks your sensitive photos behind fingerprint or face recognition, encrypts them for safety,
and works completely offline to ensure privacy. The app includes a discreet camera for taking
protected photos and appears as a normal gallery to avoid suspicion, while secretly safeguarding
your memories from prying eyes - whether from nosy friends, phone thieves, or hackers.

## Root Detection Entry Point - [`root_checker.cpp`](file:///home/ashoumar/AndroidStudioProjects/AndroidStorage/app/src/main/cpp/docs/html/root__checker_8cpp.html)
*Native method that orchestrates all security checks*

| Method                   | Description                          |
|--------------------------|--------------------------------------|
| `isDeviceRootedNative()` | JNI entry point that combines:       |
|                          | - Root binary checks                 |
|                          | - System modification detection      |
|                          | - Emulator verification              |
|                          | - Performance timing instrumentation |


## Root Detection System - [`native_root_checker.cpp`](file:///home/ashoumar/AndroidStudioProjects/AndroidStorage/app/src/main/cpp/docs/html/native__root__checker_8cpp.html) 
*C++ class for comprehensive Android root detection*

| Method                        | Detection Target                     |
|-------------------------------|--------------------------------------|
| `isRootedUsingNativeChecks()` | Main entry point for all root checks |
| `doSuperUserBinariesExist()`  | Scans for SU binaries in common paths |
| `doBusyBoxBinariesExist()`    | Detects BusyBox installations |
| `doSuperUserAppsExist()`      | Finds root management APKs |
| `isRootedUsingMagisk()`       | Identifies Magisk artifacts |
| `isSuASymlink()`              | Checks for SU symlinks |
| `isSuInEnvironmentPath()`     | Detects SU in system PATH |


## System Integrity Checker - [`native_system_integrity_verifier.cpp`](file:///home/ashoumar/AndroidStudioProjects/AndroidStorage/app/src/main/cpp/docs/html/native__system__integrity__verifier_8cpp.html)
*C++ class for detecting OS modifications and security compromises*

| Method                              | Detection Target                     |
|-------------------------------------|--------------------------------------|
| `isSystemModified()`                | Main entry point for all integrity checks |
| `doWritableSystemPartitionsExist()` | Checks for writable system partitions |
| `isBootSecurityCompromised()`       | Verifies bootloader/VBMeta state |
| `isRunningDebugBuild()`             | Detects engineering/userdebug builds |
| `isSystemDebuggable()`              | Checks ro.secure and ro.debuggable flags |
| `isSELinuxNotEnforcing()`           | Validates SELinux enforcement status |
| `isSELinuxCompromised()`            | Checks SELinux system properties |

## Emulator Detection System - [`native_emulator_checker.cpp`](file:///home/ashoumar/AndroidStudioProjects/AndroidStorage/app/src/main/cpp/docs/html/native__emulator__checker_8cpp.html) 
*Low-level C++ implementation for identifying Android emulator environments*


| Method                     | Description Target                       |
|----------------------------|------------------------------------------|
| `isDeviceEmulator()`       | Main entry point combining all checks    |
| `checkCPUArchitecture()`   | Detects x86/x86_64 CPU (emulator common) |
| `checkEmulatorProperties()` | Checks for QEMU kernel properties        |
| `checkBuildProperties()`   | Verifies model/manufacturer strings      |
| `emulatorFileExists()`     | Scans for emulator-specific files        |