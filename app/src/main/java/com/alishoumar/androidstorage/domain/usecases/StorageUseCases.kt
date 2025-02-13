package com.alishoumar.androidstorage.domain.usecases

import javax.inject.Inject

class StorageUseCases (
    val loadPhotosFromInternalStorageUseCase: LoadPhotosFromInternalStorageUseCase,
    val savePhotoInternalStorageUseCase: SavePhotoInternalStorageUseCase,
    val deletePhotoFromInternalStorageUseCase: DeletePhotoFromInternalStorageUseCase
)