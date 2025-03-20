package com.alishoumar.androidstorage.presentation.fragments.Image

import android.content.IntentSender
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.alishoumar.androidstorage.domain.usecases.externalStorage.DeletePhotoFromExternalStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val deletePhotoFromExternalStorageUseCase: DeletePhotoFromExternalStorageUseCase
): ViewModel(){

    suspend fun deletePhoto(photoUri: Uri): IntentSender?{

        return deletePhotoFromExternalStorageUseCase(photoUri)
    }
}