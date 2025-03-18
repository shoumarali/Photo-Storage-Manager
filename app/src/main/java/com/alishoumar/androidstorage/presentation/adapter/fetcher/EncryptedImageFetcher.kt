package com.alishoumar.androidstorage.presentation.adapter.fetcher

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import com.alishoumar.androidstorage.data.utils.CryptoManager
import java.io.File
import androidx.core.graphics.drawable.toDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncryptedImageFetcher (
    private val file: File,
    private val cryptoManager: CryptoManager,
    private val context: Context
): Fetcher{


    override suspend fun fetch(): FetchResult? {
        return withContext(Dispatchers.IO) {


            val encryptStream = file.inputStream()
            val decryptedBytes = cryptoManager.decrypt(encryptStream)
            val bitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
                ?: return@withContext null


             DrawableResult(
                drawable = bitmap.toDrawable(context.resources),
                isSampled = false,
                dataSource = DataSource.DISK
            )
        }
    }
}