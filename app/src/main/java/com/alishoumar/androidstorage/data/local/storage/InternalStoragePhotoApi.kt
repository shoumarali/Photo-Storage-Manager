package com.alishoumar.androidstorage.data.local.storage

import android.app.Application
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException

class InternalStoragePhotoApi (
   @ApplicationContext private val application: Application
) {
     fun savePhoto(fileName: String, bmp: Bitmap) {
        application.openFileOutput("$fileName.jpg", MODE_PRIVATE).use { stream ->
            if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                throw IOException("Couldn't save bitmap")
            }
        }
    }

    fun loadPhotos() :List<File> {
        val files = application.filesDir.listFiles()
        return files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?: listOf()
    }

    fun deletePhoto(fileName: String){
        application.deleteFile(fileName)
    }
}