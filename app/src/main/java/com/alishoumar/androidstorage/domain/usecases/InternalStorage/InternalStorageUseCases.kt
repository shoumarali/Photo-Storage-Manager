package com.alishoumar.androidstorage.domain.usecases.InternalStorage

class InternalStorageUseCases (
    val loadPhotosFromInternalStorageUseCase: LoadPhotosFromInternalStorageUseCase,
    val savePhotoInternalStorageUseCase: SavePhotoInternalStorageUseCase,
    val deletePhotoFromInternalStorageUseCase: DeletePhotoFromInternalStorageUseCase
)