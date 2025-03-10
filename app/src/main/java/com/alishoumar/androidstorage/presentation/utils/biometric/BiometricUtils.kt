package com.alishoumar.androidstorage.presentation.utils.biometric

import android.content.Context
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import kotlinx.coroutines.channels.Channel

class BiometricUtils (
    private val context: Context
){
    fun showBiometricPrompt(
        title: String,
        description: String,
        fragment: Fragment,
        resultChannel: Channel<BiometricResult>
    ){

        val manager = BiometricManager.from(context)

        val promptInfo = BiometricPrompt.PromptInfo
            .Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(getAuthenticationTypes())

        if (Build.VERSION.SDK_INT < 30){
            promptInfo.setNegativeButtonText("Cancel");
        }

        when(manager.canAuthenticate(getAuthenticationTypes())){
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->{
                resultChannel.trySend(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(BiometricResult.FeatureUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
                resultChannel.trySend(BiometricResult.AuthenticationNotSet)
                return
            }
            else -> Unit
        }

        val prompt = BiometricPrompt(
            fragment,
            object : BiometricPrompt.AuthenticationCallback(){
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    resultChannel.trySend(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    resultChannel.trySend(BiometricResult.AuthenticationFailed)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    resultChannel.trySend(BiometricResult.AuthenticationSuccess)
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