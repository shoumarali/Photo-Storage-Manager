package com.alishoumar.androidstorage.presentation

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.div

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val savePhotoToInternalStorageUseCase: SavePhotoInternalStorageUseCase,
    private val savePhotoToExternalStorageUseCase: SavePhotoToExternalStorageUseCase
) : ViewModel() {


    fun savePhotoToInternalStorage(
        filename:String,
        image: ImageProxy,
        frontCamera: Boolean
    ){
        viewModelScope.launch(Dispatchers.IO) {
            savePhotoToInternalStorageUseCase(filename, mirrorImageOrNo(image,frontCamera))
        }
    }
    fun savePhotoToExternalStorage(
        displayName:String,
        image: ImageProxy,
        frontCamera: Boolean
    ){
        viewModelScope.launch (Dispatchers.IO){
            savePhotoToExternalStorageUseCase(
                getCollection(),
                displayName,
                mirrorImageOrNo(image,frontCamera)
            )
        }
    }

    private suspend fun mirrorImageOrNo(
        image: ImageProxy,
        frontCamera: Boolean
    ): Bitmap{
        return withContext(Dispatchers.IO) {
            var bitmap = image.toBitmap()

             if (frontCamera) {
                val matrix = Matrix().apply {
                    postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
                }
                return@withContext Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }

        }
    }

    private fun getCollection(): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
    }
}