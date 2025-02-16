package com.alishoumar.androidstorage.domain.usecases.externalStorage

import android.annotation.SuppressLint
import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.net.Uri
import com.alishoumar.androidstorage.domain.repository.ExternalStorageRepository

class DeletePhotoFromExternalStorageUseCase (
    private val repo: ExternalStorageRepository
){

    @SuppressLint("NewApi")
    suspend operator fun invoke(photoUri:Uri): IntentSender?{
        try {
            repo.deletePhotoFromExternalStorage(photoUri)
            return null
        }catch (e:Exception){
            return repo.deletePhotoFromExternalStorageApi29AndAbove(
                photoUri,
                e as? RecoverableSecurityException
            )
        }
    }
}