package com.alishoumar.androidstorage.presentation.utils

import android.annotation.SuppressLint
import android.content.Context
import com.scottyab.rootbeer.RootBeer

class RootCheckUtils private constructor() {

        companion object {
            @SuppressLint("StaticFieldLeak")
            @Volatile
            private var rootBeer: RootBeer? = null

            private fun getRootBeer(context: Context): RootBeer {
                val appContext = context.applicationContext
                return rootBeer ?: synchronized(this) {
                    rootBeer ?: RootBeer(appContext).also { rootBeer = it }
                }
            }

            fun isDeviceRooted(context: Context): Boolean {
                return getRootBeer(context).isRooted
            }
        }
}