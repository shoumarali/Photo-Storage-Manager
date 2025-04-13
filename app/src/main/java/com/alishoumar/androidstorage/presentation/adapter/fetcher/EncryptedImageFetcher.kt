package com.alishoumar.androidstorage.presentation.adapter.fetcher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import com.alishoumar.androidstorage.data.utils.CryptoManager
import androidx.core.graphics.drawable.toDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class EncryptedImageFetcher(
    private val file: File,
    private val cryptoManager: CryptoManager,
    private val context: Context
) : Fetcher {

    companion object {
        private val decryptedCache = object : LruCache<String, ByteArray>(
            (Runtime.getRuntime().maxMemory() / 8).toInt()
        ) {
            override fun sizeOf(key: String, value: ByteArray) = value.size
        }
    }

    override suspend fun fetch(): FetchResult? {
        return withContext(Dispatchers.IO) {
            try {
                val cacheKey = generateCacheKey()
                val decryptedBytes = decryptedCache.get(cacheKey) ?: run {
                    decryptAndCacheFile(cacheKey)
                }

                val bitmap = decodeBitmapWithOptions(decryptedBytes)
                    ?: return@withContext null

                DrawableResult(
                    drawable = bitmap.toDrawable(context.resources),
                    isSampled = true,
                    dataSource = if (decryptedCache.get(cacheKey) != null)
                        DataSource.MEMORY_CACHE else DataSource.DISK
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun generateCacheKey(): String {
        return "${file.absolutePath}_${file.lastModified()}"
    }

    private suspend fun decryptAndCacheFile(cacheKey: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val fileBytes = ByteArray(file.length().toInt()).also {
                file.inputStream().use { stream -> stream.read(it) }
            }
            cryptoManager.decrypt(fileBytes.inputStream()).also { decrypted ->
                decryptedCache.put(cacheKey, decrypted)
            }
        }
    }

    private fun decodeBitmapWithOptions(decryptedBytes: ByteArray): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size, options)

        options.apply {
            inJustDecodeBounds = false
            inSampleSize = calculateInSampleSize(this, 344, 344)
            inPreferredConfig = Bitmap.Config.RGB_565
            inMutable = false
        }

        return BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}