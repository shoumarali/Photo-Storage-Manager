package com.alishoumar.androidstorage.presentation

import android.content.IntentSender
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.domain.usecases.externalStorage.DeletePhotoFromExternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.externalStorage.LoadPhotosFromExternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.externalStorage.SavePhotoToExternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.permissions.GetUnGrantedPermissionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExternalStorageViewModel @Inject constructor(
    private val loadPhotosFromExternalStorageUseCase: LoadPhotosFromExternalStorageUseCase,
    private val savePhotoToExternalStorageUseCase: SavePhotoToExternalStorageUseCase,
    private val deletePhotoFromExternalStorageUseCase: DeletePhotoFromExternalStorageUseCase,
    private val getUnGrantedPermissionsUseCase: GetUnGrantedPermissionsUseCase
) :ViewModel(){

    init {
        loadPhotosFromExternalStorage()
    }

    private val _externalStoragePhotos = MutableLiveData<List<ExternalStoragePhoto>>()
    val externalStoragePhotos: LiveData<List<ExternalStoragePhoto>> = _externalStoragePhotos

    private val _unGrantedPermissions = MutableLiveData<List<String>>(getUnGrantedPermissionsUseCase())
    val unGrantedPermissions : LiveData<List<String>> = _unGrantedPermissions

    fun loadPhotosFromExternalStorage(){
        if(getUnGrantedPermissionsUseCase().isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val photos = loadPhotosFromExternalStorageUseCase(getCollection())
                withContext(Dispatchers.Main) {
                    _externalStoragePhotos.value = photos
                }
            }
        }else{
            setUnGrantedPermissions()
        }
    }

    fun savePhotoToExternalStorage(
        displayName:String,
        bitmap: Bitmap
    ){
        if(getUnGrantedPermissionsUseCase().isEmpty()) {
            viewModelScope.launch {
                savePhotoToExternalStorageUseCase(
                    getCollection(),
                    displayName,
                    bitmap
                )
                loadPhotosFromExternalStorage()
            }
        }else{
            setUnGrantedPermissions()
        }
    }

    suspend fun deletePhotoFromExternalStorage(photoUri: Uri): IntentSender?{
        return deletePhotoFromExternalStorageUseCase(photoUri)
    }

     private fun setUnGrantedPermissions(){
        _unGrantedPermissions.value = getUnGrantedPermissionsUseCase()
    }

   private fun getCollection(): Uri {
       return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
       } else {
           MediaStore.Images.Media.EXTERNAL_CONTENT_URI
       }
   }
}