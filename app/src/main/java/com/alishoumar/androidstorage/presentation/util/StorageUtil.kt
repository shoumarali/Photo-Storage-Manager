package com.alishoumar.androidstorage.presentation.util

import android.os.Build


inline fun <T> sdk29AndAbove(onSdk29:() ->T): T?{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
     onSdk29()
    }else{
        null
    }
}