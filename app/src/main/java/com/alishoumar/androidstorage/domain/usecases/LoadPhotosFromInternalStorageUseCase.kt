package com.alishoumar.androidstorage.domain.usecases

import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto
import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository
import javax.inject.Inject

class LoadPhotosFromInternalStorageUseCase(
    private val repository: InternalStorageRepository
) {
    suspend operator fun invoke(): List<InternalStoragePhoto>{
        return repository.loadPhotos()
    }
}