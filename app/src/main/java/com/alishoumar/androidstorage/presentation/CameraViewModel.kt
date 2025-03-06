package com.alishoumar.androidstorage.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alishoumar.androidstorage.domain.usecases.externalStorage.DeletePhotoFromExternalStorageUseCase
import com.alishoumar.androidstorage.domain.usecases.externalStorage.SavePhotoToExternalStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val savePhotoToInternalStorageUseCase: DeletePhotoFromExternalStorageUseCase,
    private val savePhotoToExternalStorageUseCase: SavePhotoToExternalStorageUseCase
) : ViewModel() {

    private val _privateModeEnabled = MutableLiveData<Boolean>(false)
    val privateModeEnabled: LiveData<Boolean> = _privateModeEnabled

    suspend fun savePhoto(){

    }
}