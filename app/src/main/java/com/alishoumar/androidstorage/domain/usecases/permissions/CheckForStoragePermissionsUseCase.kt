package com.alishoumar.androidstorage.domain.usecases.permissions

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext

class CheckForStoragePermissionsUseCase (
    @ApplicationContext private val application: Application
){

    operator fun invoke(
        permissionResultLauncher: () -> Unit
    ) {

        val hasReadPermission = ContextCompat
            .checkSelfPermission(
                application,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat
            .checkSelfPermission(
                application,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val permissionsToRequest = mutableListOf<String>()
        if (!hasReadPermission) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!(hasWritePermission || minSdk29)) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionResultLauncher()
        }

    }
}