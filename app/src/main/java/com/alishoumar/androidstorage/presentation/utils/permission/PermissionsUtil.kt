package com.alishoumar.androidstorage.presentation.utils.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionsUtil {

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

    fun getUnGrantedPermissions(context: Context):List<String>{
        val permissions = when {
            Build.VERSION.SDK_INT in Build.VERSION_CODES.Q..Build.VERSION_CODES.S_V2 -> {
                requiredPermissionsBetween29And32
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                requiredPermissionAbove32
            }
            else ->
                requiredPermissionsBetween24And28
        }
        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }
}