package com.alishoumar.androidstorage.domain.usecases.externalStorage

import android.graphics.Bitmap
import android.net.Uri
import com.alishoumar.androidstorage.domain.repository.ExternalStorageRepository

class SavePhotoToExternalStorageUseCase (
    private val repo: ExternalStorageRepository
) {

    suspend operator fun invoke(
        collection: Uri,
        displayName: String,
        bitmap: Bitmap
        ):Boolean{
        return try {
            repo.savePhotoToExternalStorage(
                collection,
                displayName,
                bitmap
            )
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }
}