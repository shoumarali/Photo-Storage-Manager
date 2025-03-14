package com.alishoumar.androidstorage.presentation.fragments.Biometric

import androidx.lifecycle.ViewModel
import com.alishoumar.androidstorage.presentation.utils.biometric.BiometricResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class BiometricViewModel : ViewModel() {

    private val _channel = Channel<BiometricResult>()
    val biometricFlow: Flow<BiometricResult> = _channel.receiveAsFlow()

    fun sendBiometricResult(result: BiometricResult){
        _channel.trySend(result)
    }
}