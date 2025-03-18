package com.alishoumar.androidstorage.data.mappers

import android.graphics.BitmapFactory
import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto
import java.io.File


fun List<File>.toListInternalStoragePhotos():List<InternalStoragePhoto>{
    return this.map {
//        val bytes = it.readBytes()
//        val bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
        InternalStoragePhoto(
            name = it.name,
            filePath = it.absolutePath
        )
    }
}