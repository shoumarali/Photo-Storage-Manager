package com.alishoumar.androidstorage.domain.models

import android.graphics.Bitmap

data class InternalStoragePhoto(
    val name:String,
    val bmp: Bitmap,
    val filePath: String
)