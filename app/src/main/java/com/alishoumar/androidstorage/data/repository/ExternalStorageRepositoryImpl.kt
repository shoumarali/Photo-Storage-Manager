package com.alishoumar.androidstorage.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.alishoumar.androidstorage.data.local.storage.ExternalStoragePhotoApi
import com.alishoumar.androidstorage.data.mappers.toExternalStoragePhoto
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.domain.repository.ExternalStorageRepository
import javax.inject.Inject

class ExternalStorageRepositoryImpl @Inject constructor(
    private val api: ExternalStoragePhotoApi
) : ExternalStorageRepository{


    override suspend fun loadPhotosFromExternalStorage(collection: Uri): List<ExternalStoragePhoto> {
        return api.loadPhotosFromExternalStorage(collection).toExternalStoragePhoto()
    }

    override suspend fun savePhotoToExternalStorage(
        collection: Uri,
        displayName: String,
        bitmap: Bitmap
    ){
        api.savePhotoToExternalStorage(collection, displayName, bitmap)
    }
}