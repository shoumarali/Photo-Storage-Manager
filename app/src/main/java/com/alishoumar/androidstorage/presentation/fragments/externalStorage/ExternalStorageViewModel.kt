package com.alishoumar.androidstorage.presentation.fragments.externalStorage

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.domain.usecases.externalStorage.LoadPhotosFromExternalStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ExternalStorageViewModel @Inject constructor(
    private val loadPhotosFromExternalStorageUseCase: LoadPhotosFromExternalStorageUseCase
) :ViewModel(){

    init {
        loadPhotosFromExternalStorage()
    }

    private val _externalStoragePhotos = MutableLiveData<List<ExternalStoragePhoto>>()
    val externalStoragePhotos: LiveData<List<ExternalStoragePhoto>> = _externalStoragePhotos

    fun loadPhotosFromExternalStorage(){
            viewModelScope.launch(Dispatchers.IO) {
                val photos = loadPhotosFromExternalStorageUseCase(getCollection())
                withContext(Dispatchers.Main) {
                    _externalStoragePhotos.value = photos
                }
            }
    }

   private fun getCollection(): Uri {
       return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
       } else {
           MediaStore.Images.Media.EXTERNAL_CONTENT_URI
       }
   }
}