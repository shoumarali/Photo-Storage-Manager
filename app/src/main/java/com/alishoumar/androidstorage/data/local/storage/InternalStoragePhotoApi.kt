package com.alishoumar.androidstorage.data.local.storage

import android.app.Application
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import com.alishoumar.androidstorage.data.utils.CryptoManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class InternalStoragePhotoApi (
   @ApplicationContext private val application: Application,
    private val cryptoManager: CryptoManager
) {
     fun savePhoto(fileName: String, bmp: Bitmap) {

         val byteArrayOutputStream = ByteArrayOutputStream()
         if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, byteArrayOutputStream)) {
             throw IOException("Couldn't save bitmap")
         }
         val imagesBytes= byteArrayOutputStream.toByteArray()

        application.openFileOutput("$fileName.enc", MODE_PRIVATE).use { outputStream ->
            cryptoManager.encrypt(imagesBytes, outputStream)
        }
    }

    fun loadPhotos() :List<File> {
        val files = application.filesDir.listFiles()
        return files?.filter { it.canRead() && it.isFile && it.name.endsWith(".enc") }?: listOf()
    }

    fun deletePhoto(fileName: String){
        application.deleteFile(fileName)
    }
}