package com.alishoumar.androidstorage.presentation.utils.biometric

sealed interface BiometricResult {
    data object HardwareUnavailable:BiometricResult
    data object FeatureUnavailable:BiometricResult
    data object AuthenticationFailed:BiometricResult
    data class AuthenticationError(val error:String) : BiometricResult
    data object AuthenticationSuccess:BiometricResult
    data object AuthenticationNotSet:BiometricResult
}