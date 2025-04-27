package com.alishoumar.androidstorage.presentation.fragments.shared

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageRefreshViewModel @Inject constructor() : ViewModel() {

    private val _internalStorageChanged = MutableLiveData<Unit>()
    val internalStorageChanged: LiveData<Unit> = _internalStorageChanged

    fun notifyInternalStorageChanged() {
        _internalStorageChanged.value = Unit
    }
}