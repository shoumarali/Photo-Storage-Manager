package com.alishoumar.androidstorage.domain.usecases.InternalStorage

import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository

class DeletePhotoFromInternalStorageUseCase(
    private val repository: InternalStorageRepository
) {
    suspend operator fun invoke(fileName: String): Boolean{
        try {
            repository.deletePhoto(fileName)
            return true
        }catch (e: Exception){
            e.printStackTrace()
            return false
        }
    }
}