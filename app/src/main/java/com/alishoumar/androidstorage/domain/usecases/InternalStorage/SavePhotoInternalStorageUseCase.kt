package com.alishoumar.androidstorage.domain.usecases.InternalStorage

import android.graphics.Bitmap
import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository
import java.io.IOException

class SavePhotoInternalStorageUseCase (
    private val repository: InternalStorageRepository
) {
    suspend operator fun invoke(fileName: String, bitmap: Bitmap):Boolean{
        try {
            repository.savePhoto(fileName,bitmap)
            return true
        }catch (e:IOException){
            e.printStackTrace()
            return false
        }
    }
}