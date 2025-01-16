package com.alishoumar.androidstorage.data

import android.graphics.Bitmap
import android.net.Uri

data class ExternalStoragePhoto(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val uri:Uri
)