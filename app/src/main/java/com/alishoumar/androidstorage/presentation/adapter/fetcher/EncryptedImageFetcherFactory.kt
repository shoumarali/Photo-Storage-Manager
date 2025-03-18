package com.alishoumar.androidstorage.presentation.adapter.fetcher

import android.content.Context
import coil.ImageLoader
import coil.fetch.Fetcher
import coil.request.Options
import com.alishoumar.androidstorage.data.utils.CryptoManager
import java.io.File

class EncryptedImageFetcherFactory(
    private val cryptoManager: CryptoManager,
    private val context: Context
): Fetcher.Factory<File> {

    override fun create(
        data: File,
        options: Options,
        imageLoader: ImageLoader
    ): Fetcher? {

        return EncryptedImageFetcher(
            data,
            cryptoManager,
            context
        )
    }

}