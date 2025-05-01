package com.alishoumar.androidstorage.presentation.fragments.privateImage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alishoumar.androidstorage.domain.usecases.InternalStorage.DeletePhotoFromInternalStorageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PrivateImageViewModel @Inject constructor(
    private val deletePhotoFromInternalStorageUseCase: DeletePhotoFromInternalStorageUseCase
) : ViewModel() {

    fun deletePhotoFromInternalStorage(
        fileName: String,
        onSuccess : (Boolean) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO) {
            deletePhotoFromInternalStorageUseCase(fileName)
            withContext(Dispatchers.Main){
                onSuccess(true)
            }
        }
    }

}