package com.alishoumar.androidstorage.presentation.utils

import android.os.Build
import java.time.LocalDate
import java.util.Calendar
import java.util.UUID

object FileUtils {

     fun createFileName(): String {
        val (year, month, day) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().let { Triple(it.year, it.monthValue, it.dayOfMonth) }
        } else {
            Calendar.getInstance().let {
                Triple(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
            }
        }

        return "StorageApp_%04d%02d%02d_%s".format(year, month, day, UUID.randomUUID())
    }

}