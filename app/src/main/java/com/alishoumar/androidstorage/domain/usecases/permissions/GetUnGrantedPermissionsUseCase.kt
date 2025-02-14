package com.alishoumar.androidstorage.domain.usecases.permissions

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import com.alishoumar.androidstorage.domain.utils.PermissionUtil
import dagger.hilt.android.qualifiers.ApplicationContext

class GetUnGrantedPermissionsUseCase (
    @ApplicationContext private val application:Application
){

    operator fun invoke(): List<String>{

        val permissions = when {
            Build.VERSION.SDK_INT in Build.VERSION_CODES.Q..Build.VERSION_CODES.S_V2 -> {
                PermissionUtil.requiredPermissionsBetween29And32
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                PermissionUtil.requiredPermissionAbove32
            }
            else ->
                PermissionUtil.requiredPermissionsBetween24And28
        }
        return permissions.filter {
            application.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
    }
}