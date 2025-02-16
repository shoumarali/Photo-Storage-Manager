package com.alishoumar.androidstorage.data.local.storage

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.ContentValues
import android.content.IntentSender
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.alishoumar.androidstorage.data.local.storage.dao.ExternalPhotoDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException

class ExternalStoragePhotoApi(
    @ApplicationContext private val application: Application
) {


    fun loadPhotosFromExternalStorage(
        collection: Uri
    ): List<ExternalPhotoDto> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        val photos = mutableListOf<ExternalPhotoDto>()

        application.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        ).use { cursor ->

            if (cursor != null) {

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add(ExternalPhotoDto(id, displayName, width, height, contentUri))
                }
            }
            return photos.toList()
        }
    }

    fun savePhotoToExternalStorage(
        collection: Uri,
        displayName: String,
        bmp: Bitmap
    ) {

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }
        application.contentResolver.insert(collection, contentValues)?.also { uri ->
            application.contentResolver.openOutputStream(uri)?.use {
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, it)) {
                    throw IOException("Couldn't save the bitmap")
                }
            }
        } ?: throw IOException("Couldn't create media store entry")
    }

    fun deletePhotoFromExternalStorage(photoUri: Uri){
        application.contentResolver.delete(photoUri,null,null)
    }

    fun deletePhotoFromExternalStorageApi29AndAbove(
        photoUri:Uri,
        recoverableSecurityException: RecoverableSecurityException?
    ): IntentSender?{
        return when{
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                MediaStore.createDeleteRequest(
                    application.contentResolver,
                    listOf(photoUri)
                ).intentSender
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->{
                recoverableSecurityException?.userAction?.actionIntent?.intentSender
            }
            else -> null
        }
    }
}