package com.alishoumar.androidstorage.domain.utils

import android.Manifest

object PermissionUtil {
    val requiredPermissionsBetween24And28 = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    val requiredPermissionsBetween29And32 = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val requiredPermissionAbove32= listOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )
}