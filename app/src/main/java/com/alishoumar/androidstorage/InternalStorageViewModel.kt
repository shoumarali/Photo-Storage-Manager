package com.alishoumar.androidstorage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alishoumar.androidstorage.domain.models.InternalStoragePhoto
import com.alishoumar.androidstorage.domain.usecases.StorageUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class Test @Inject constructor() : ViewModel() {

//    val useCases = StorageUseCases()
//
    private val _internalPhotos= MutableLiveData<List<InternalStoragePhoto>>()
    val internalPhotos: LiveData<List<InternalStoragePhoto>> = _internalPhotos
//
//    init {
//        loadPhotosFromInternalStorage()
//    }
//
//    private fun loadPhotosFromInternalStorage(){
//        viewModelScope.launch(Dispatchers.IO){
//            val data = useCases.loadPhotosFromInternalStorageUseCase()
//            withContext(Dispatchers.Main){
//                _internalPhotos.value = data
//            }
//        }
//    }

//    fun deletePhotoFromInternalStorage(fileName: String){
//        viewModelScope.launch (Dispatchers.IO){
//            useCases.deletePhotoFromInternalStorageUseCase(fileName)
//        }
//        loadPhotosFromInternalStorage()
//    }
//
//    fun savePhotoToInternalStorage(fileName: String, bitmap: Bitmap){
//        viewModelScope.launch (Dispatchers.IO){
//            useCases.savePhotoInternalStorageUseCase(fileName, bitmap)
//        }
//        loadPhotosFromInternalStorage()
//    }
}