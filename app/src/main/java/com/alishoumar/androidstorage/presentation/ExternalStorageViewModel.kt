package com.alishoumar.androidstorage.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.domain.usecases.externalStorage.LoadPhotosFromExternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.externalStorage.SavePhotoToExternalStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExternalStorageViewModel @Inject constructor(
    private val loadPhotosFromExternalStorageUseCase: LoadPhotosFromExternalStorageUseCase,
    private val savePhotoToExternalStorageUseCase: SavePhotoToExternalStorageUseCase
) :ViewModel(){


    private val _isReadPermissionGranted = MutableLiveData(false)
    val isReadPermissionGranted: LiveData<Boolean> = _isReadPermissionGranted

    private val _isWritePermissionGranted = MutableLiveData(false)
    private val isWritePermissionGranted: LiveData<Boolean> = _isWritePermissionGranted

    private val _externalStoragePhotos = MutableLiveData<List<ExternalStoragePhoto>>()
    val externalStoragePhotos: LiveData<List<ExternalStoragePhoto>> = _externalStoragePhotos


    fun loadPhotosFromExternalStorage(collection: Uri){
        viewModelScope.launch(Dispatchers.IO) {
            val photos = loadPhotosFromExternalStorageUseCase(collection)
            withContext(Dispatchers.Main) {
                _externalStoragePhotos.value = photos
            }
        }
    }

    fun savePhotoToExternalStorage(
        collection: Uri,
        displayName:String,
        bitmap: Bitmap
    ){
        viewModelScope.launch {
           savePhotoToExternalStorageUseCase(
                collection,
                displayName,
                bitmap
            )
            loadPhotosFromExternalStorageUseCase(collection)
        }
    }

    fun initializeContentObserver(){

    }
}