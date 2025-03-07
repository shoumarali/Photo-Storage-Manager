package com.alishoumar.androidstorage.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alishoumar.androidstorage.domain.usecases.InternalStorage.SavePhotoInternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.externalStorage.SavePhotoToExternalStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val savePhotoToInternalStorageUseCase: SavePhotoInternalStorageUseCase,
    private val savePhotoToExternalStorageUseCase: SavePhotoToExternalStorageUseCase
) : ViewModel() {

    private val _privateModeEnabled = MutableLiveData<Boolean>(false)
    val privateModeEnabled: LiveData<Boolean> = _privateModeEnabled

    fun savePhotoToInternalStorage(filename:String,bitmap: Bitmap){
        viewModelScope.launch(Dispatchers.IO) {
            savePhotoToInternalStorageUseCase(filename, bitmap)
        }
    }
    fun savePhotoToExternalStorage(
        collectionUri: Uri,
        displayName:String,
        bitmap: Bitmap
    ){
        viewModelScope.launch (Dispatchers.IO){
            savePhotoToExternalStorageUseCase(
                collectionUri,
                displayName,
                bitmap
            )
        }
    }
}