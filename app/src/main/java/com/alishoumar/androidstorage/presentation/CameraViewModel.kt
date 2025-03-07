package com.alishoumar.androidstorage.presentation

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.camera.core.ImageProxy
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
import kotlin.div

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val savePhotoToInternalStorageUseCase: SavePhotoInternalStorageUseCase,
    private val savePhotoToExternalStorageUseCase: SavePhotoToExternalStorageUseCase
) : ViewModel() {

    private val _privateModeEnabled = MutableLiveData<Boolean>(false)
    val privateModeEnabled: LiveData<Boolean> = _privateModeEnabled

    fun savePhotoToInternalStorage(
        filename:String,
        image: ImageProxy,
        frontCamera: Boolean
    ){
        viewModelScope.launch(Dispatchers.IO) {
            var bitmap = image.toBitmap()

            // Mirror image if it's from the front camera
            bitmap = if (frontCamera) {
                val matrix = Matrix().apply {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

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