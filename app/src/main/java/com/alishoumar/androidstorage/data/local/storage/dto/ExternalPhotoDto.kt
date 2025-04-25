package com.alishoumar.androidstorage.data.local.storage.dto

import android.net.Uri

data class ExternalPhotoDto(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val uri: Uri
)