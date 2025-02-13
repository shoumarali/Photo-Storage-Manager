package com.alishoumar.androidstorage.domain.models

import android.net.Uri

data class ExternalStoragePhoto(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val uri:Uri
)