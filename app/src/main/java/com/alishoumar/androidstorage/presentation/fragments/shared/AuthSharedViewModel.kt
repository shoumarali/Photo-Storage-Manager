package com.alishoumar.androidstorage.presentation.fragments.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthSharedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _isAuthenticated = savedStateHandle.getLiveData<Boolean>(
        "isAuthenticated",false
    )
    val isAuthenticated: LiveData<Boolean>  = _isAuthenticated

    fun setAuthentication(isAuthenticated: Boolean){
        _isAuthenticated.value = isAuthenticated
        savedStateHandle["isAuthenticated"] = isAuthenticated
    }
}