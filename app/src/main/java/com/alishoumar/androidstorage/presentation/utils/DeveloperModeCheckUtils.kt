package com.alishoumar.androidstorage.presentation.utils

import android.content.Context
import android.os.Build
import android.os.UserManager
import android.provider.Settings

object DeveloperModeCheckUtils {

    fun isDevelopmentSettingsEnabled(context: Context): Boolean {
        val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

        val settingEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            if (Build.TYPE == "eng") 1 else 0
        ) != 0

        val hasRestriction = userManager.hasUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES)

        val isAdmin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            userManager.isAdminUser
        } else {
            true
        }
        return isAdmin && !hasRestriction && settingEnabled
    }
}
