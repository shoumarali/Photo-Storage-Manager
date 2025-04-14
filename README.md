# VaultPic 🔒📸

A secure photo vault that protects your private pictures with biometric authentication and military-grade encryption.

## 📑 Table of Contents
- [Summary](#-summary)
- [Overview](#-overview)
- [Key Features](#-key-features)
- [Technical Stack](#-technical-stack)
- [Root Checker System](#-root-checker-system)

## 📌 Summary
VaultPic is a privacy-focused Android application that:
- 🔐 Locks photos behind biometric authentication (fingerprint/face recognition)
- 🏦 Encrypts media using AES-256 encryption
- 📷 Includes a discreet camera for capturing protected photos
- 🚫 Works completely offline for maximum privacy

## 🌐 Overview
VaultPic protects your sensitive photos from:
- 👀 Nosy friends/family
- 📱 Phone thieves
- 🕵️‍♂️ Hackers
- 🤖 Malware/Spyware

The app provides multiple layers of security including biometric authentication, filesystem encryption, and anti-tampering measures while maintaining a user-friendly interface.

## ✨ Key Features
| Feature | Description |
|---------|-------------|
| **Biometric Lock** | Fingerprint/Face ID protection for all access |
| **Military-Grade Encryption** | AES-256 encrypted storage |
| **Secure Camera** | Built-in camera that saves directly to vault |
| **Offline Operation** | No internet permissions required |
| **Memory Optimization** | Smart caching and downsampling |
| **Anti-Tampering** | Root/emulator detection |

## 💻 Technical Stack
- **Language**: Kotlin
- **DI**: Dagger-Hilt
- **Camera**: CameraX
- **Image Loading**: Coil with custom secure fetcher
- **Navigation**: Jetpack Navigation Component
- **CI/CD**: GitHub Actions
- **Native Components**: C++ (for security checks)

## 🔍 Root Checker System
VaultPic employs multiple layers of protection to detect and defend against rooted or jailbroken devices.
Learn more in the documentation: https://shoumarali.github.io/vault-pic/
