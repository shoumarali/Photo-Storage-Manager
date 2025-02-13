package com.alishoumar.androidstorage.data.repository

import android.graphics.Bitmap
import com.alishoumar.androidstorage.data.local.storage.InternalStoragePhotoApi
import com.alishoumar.androidstorage.data.mappers.toListInternalStoragePhotos
import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto
import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository
import javax.inject.Inject

class InternalStorageRepositoryImpl @Inject constructor(
    private val internalStoragePhotoApi: InternalStoragePhotoApi
) : InternalStorageRepository {

    override suspend fun savePhoto(fileName: String, bitmap: Bitmap) {
        internalStoragePhotoApi.savePhoto(fileName, bitmap)
    }

    override suspend fun loadPhotos(): List<InternalStoragePhoto> {
        return internalStoragePhotoApi.loadPhotos().toListInternalStoragePhotos()
    }

    override suspend fun deletePhoto(fileName: String) {
        internalStoragePhotoApi.deletePhoto(fileName)
    }


}