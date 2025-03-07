package com.alishoumar.androidstorage.presentation

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto
import com.alishoumar.androidstorage.domain.usecases.InternalStorage.InternalStorageUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class InternalStorageViewModel @Inject constructor(
    private val useCases: InternalStorageUseCases
) : ViewModel() {


    private val _internalPhotos= MutableLiveData<List<InternalStoragePhoto>>()
    val internalPhotos: LiveData<List<InternalStoragePhoto>> = _internalPhotos

    init {
        loadPhotosFromInternalStorage()
    }

     private fun loadPhotosFromInternalStorage(){
        viewModelScope.launch(Dispatchers.IO){
//            delay(200)
            val data = useCases.loadPhotosFromInternalStorageUseCase()
            withContext(Dispatchers.Main){
                _internalPhotos.value = data
            }
        }
    }

    fun deletePhotoFromInternalStorage(fileName: String){
        viewModelScope.launch (Dispatchers.IO){
            useCases.deletePhotoFromInternalStorageUseCase( fileName)
        }
        loadPhotosFromInternalStorage()
    }

    fun savePhotoToInternalStorage( fileName: String, bitmap: Bitmap){
        viewModelScope.launch (Dispatchers.IO){
            useCases.savePhotoInternalStorageUseCase(fileName, bitmap)
        }
        loadPhotosFromInternalStorage()
    }
}