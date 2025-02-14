package com.alishoumar.androidstorage.domain.usecases.InternalStorage

import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto
import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository

class LoadPhotosFromInternalStorageUseCase(
    private val repository: InternalStorageRepository
) {
    suspend operator fun invoke(): List<InternalStoragePhoto>{
        return repository.loadPhotos()
    }
}