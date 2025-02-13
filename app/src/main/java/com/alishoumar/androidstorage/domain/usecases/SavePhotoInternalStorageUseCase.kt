package com.alishoumar.androidstorage.domain.usecases

import android.graphics.Bitmap
import com.alishoumar.androidstorage.domain.repository.InternalStorageRepository
import java.io.IOException
import javax.inject.Inject

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