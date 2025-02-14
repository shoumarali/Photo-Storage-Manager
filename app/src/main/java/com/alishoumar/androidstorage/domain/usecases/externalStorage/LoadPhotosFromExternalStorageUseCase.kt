package com.alishoumar.androidstorage.domain.usecases.externalStorage

import android.net.Uri
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.domain.repository.ExternalStorageRepository

class LoadPhotosFromExternalStorageUseCase (
    private val repo:ExternalStorageRepository
) {
    suspend operator fun invoke(collection: Uri): List<ExternalStoragePhoto>{
        return repo.loadPhotosFromExternalStorage(collection)
    }
}