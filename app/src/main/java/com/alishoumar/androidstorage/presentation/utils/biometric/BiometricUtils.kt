package com.alishoumar.androidstorage.presentation.utils.biometric

import android.content.Context
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment

class BiometricUtils (
    private val context: Context
){
    fun showBiometricPrompt(
        title: String,
        description: String,
        fragment: Fragment,
        sendBiometricResult: (BiometricResult) -> Unit
    ){

        val manager = BiometricManager.from(context)

        val promptInfo = BiometricPrompt.PromptInfo
            .Builder()
            .setTitle(title)
            .setDescription(description)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfo.setAllowedAuthenticators(getAuthenticationTypes())
        } else {
            promptInfo.setNegativeButtonText("Cancel")
        }

        when(manager.canAuthenticate(getAuthenticationTypes())){
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                sendBiometricResult(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                sendBiometricResult(BiometricResult.FeatureUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                sendBiometricResult(BiometricResult.AuthenticationNotSet)
                return
            }
            else -> Unit
        }

        val activity = fragment.activity ?: return

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    sendBiometricResult(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    sendBiometricResult(BiometricResult.AuthenticationFailed)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    sendBiometricResult(BiometricResult.AuthenticationSuccess)
                }
            }
        )
        prompt.authenticate(promptInfo.build())
    }

    private fun getAuthenticationTypes(): Int{
        return  when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            }
            else -> {
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            }
        }
    }
}