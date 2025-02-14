package com.alishoumar.androidstorage.data.mappers

import com.alishoumar.androidstorage.data.local.storage.dao.ExternalPhotoDto
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto

fun List<ExternalPhotoDto>.toExternalStoragePhoto(): List<ExternalStoragePhoto> {
    return this.map {
        ExternalStoragePhoto(
            it.id,
            it.name,
            it.width,
            it.height,
            it.uri
        )
    }
}