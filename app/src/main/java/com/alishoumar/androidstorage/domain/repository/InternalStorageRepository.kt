package com.alishoumar.androidstorage.domain.repository

import android.graphics.Bitmap
import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto

interface InternalStorageRepository {

    suspend fun savePhoto(fileName:String, bitmap: Bitmap)

    suspend fun loadPhotos() : List<InternalStoragePhoto>

    suspend fun deletePhoto(fileName: String)
}